package com.labresa.controller;

import com.labresa.Main;
import com.labresa.Navigator;
import com.labresa.Session;
import com.labresa.model.Notification;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.patterns.ReservationBuilder;
import com.labresa.patterns.approval.ApprovalResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class DashboardController {

    @FXML private Label currentUserLabel;

    // Booking wizard
    @FXML private javafx.scene.layout.VBox bookingStep1;
    @FXML private javafx.scene.layout.VBox bookingStep2;

    @FXML private TableView<Resource> resourceTable;
    @FXML private TableColumn<Resource, String> colResName;
    @FXML private TableColumn<Resource, String> colResType;
    @FXML private TableColumn<Resource, Double> colResCost;
    @FXML private TableColumn<Resource, String> colResStatus;
    @FXML private TableColumn<Resource, String> colResUsage;

    @FXML private ComboBox<Resource> resourceCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private CheckBox recurringCheck;
    @FXML private TextField notesField;
    @FXML private Label step1ErrorLabel;

    @FXML private Label confirmResourceLabel;
    @FXML private Label confirmDatesLabel;
    @FXML private Label confirmCostLabel;
    @FXML private Label confirmNotesLabel;
    @FXML private Label requestResultLabel;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> colResvId;
    @FXML private TableColumn<Reservation, Integer> colResvResource;
    @FXML private TableColumn<Reservation, String> colResvStart;
    @FXML private TableColumn<Reservation, String> colResvEnd;
    @FXML private TableColumn<Reservation, String> colResvStatus;
    @FXML private Label actionResultLabel;

    @FXML private ListView<String> notificationList;
    @FXML private TextArea reportArea;

    // Pending draft between "Save & Continue" and "Confirm Booking"
    private Resource pendingResource;
    private LocalDateTime pendingStart;
    private LocalDateTime pendingEnd;

    @FXML
    public void initialize() {
        currentUserLabel.setText(Session.getCurrentUser().getName() + " (" + Session.getCurrentUser().getRole() + ")");

        colResName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colResType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colResCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colResStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colResUsage.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                r.getValue().getUsageCounter() + "/" + r.getValue().getMaintenanceThreshold()));

        colResvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colResvResource.setCellValueFactory(new PropertyValueFactory<>("resourceId"));
        colResvStart.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(r.getValue().getStartTime())));
        colResvEnd.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(r.getValue().getEndTime())));
        colResvStatus.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(r.getValue().getStatus())));

        resourceCombo.setCellFactory(cb -> resourceListCell());
        resourceCombo.setButtonCell(resourceListCell());

        // Real calendar popups; no time entry needed - bookings run full calendar days.
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());

        refreshAll();
    }

    private ListCell<Resource> resourceListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Resource r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? "" : r.getId() + " - " + r.getName() + " ($" + r.getCost() + ")");
            }
        };
    }

    private void refreshAll() {
        List<Resource> resources = Main.getFacade().getResourceCatalog();
        resourceTable.setItems(FXCollections.observableArrayList(resources));
        resourceCombo.setItems(FXCollections.observableArrayList(resources));

        List<Reservation> mine = Main.getFacade().getAllReservations().stream()
                .filter(r -> r.getUserId() == Session.getCurrentUser().getId())
                .toList();
        reservationTable.setItems(FXCollections.observableArrayList(mine));

        List<Notification> notes = Main.getFacade().getNotifications(Session.getCurrentUser().getId());
        ObservableList<String> lines = FXCollections.observableArrayList();
        for (Notification n : notes) {
            lines.add("[" + n.getType() + "] " + n.getMessage());
        }
        notificationList.setItems(lines);
    }

    // ---------------- Booking wizard: Step 1 -> Save & Continue ----------------

    @FXML
    private void handleSaveAndContinue() {
        hideError(step1ErrorLabel);

        Resource resource = resourceCombo.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (resource == null) {
            showError(step1ErrorLabel, "Please select a resource.");
            return;
        }
        if (start == null || end == null) {
            showError(step1ErrorLabel, "Please choose both a start and end date.");
            return;
        }
        if (end.isBefore(start)) {
            showError(step1ErrorLabel, "End date cannot be before the start date.");
            return;
        }

        pendingResource = resource;
        pendingStart = start.atStartOfDay();
        pendingEnd = end.atTime(LocalTime.of(23, 59));

        confirmResourceLabel.setText("Resource: " + resource.getName() + " (" + resource.getType() + ")");
        confirmDatesLabel.setText("Dates: " + start + " to " + end);
        confirmCostLabel.setText("Cost: $" + resource.getCost());
        confirmNotesLabel.setText("Notes: " + (notesField.getText().isBlank() ? "(none)" : notesField.getText()));
        requestResultLabel.setText("");

        bookingStep1.setVisible(false);
        bookingStep1.setManaged(false);
        bookingStep2.setVisible(true);
        bookingStep2.setManaged(true);
    }

    @FXML
    private void handleBackToStep1() {
        bookingStep2.setVisible(false);
        bookingStep2.setManaged(false);
        bookingStep1.setVisible(true);
        bookingStep1.setManaged(true);
    }

    @FXML
    private void handleConfirmBooking() {
        try {
            Reservation reservation = new ReservationBuilder(
                    pendingResource.getId(), Session.getCurrentUser().getId(),
                    Session.getCurrentUser().getRole(), pendingStart, pendingEnd)
                    .recurring(recurringCheck.isSelected())
                    .notes(notesField.getText())
                    .build();

            ApprovalResult result = Main.getFacade().requestReservation(reservation);
            requestResultLabel.setText("Result: " + result);
            refreshAll();
        } catch (Exception e) {
            requestResultLabel.setText("Error: " + e.getMessage());
        }
    }

    // ---------------- My Reservations tab ----------------

    @FXML
    private void handleCheckIn() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) { actionResultLabel.setText("Select a reservation first."); return; }
        try {
            Main.getFacade().checkIn(selected.getId());
            actionResultLabel.setText("Checked in reservation #" + selected.getId());
            refreshAll();
        } catch (Exception e) {
            actionResultLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) { actionResultLabel.setText("Select a reservation first."); return; }
        try {
            Main.getFacade().checkOut(selected.getId(), "Returned in good condition.");
            actionResultLabel.setText("Checked out reservation #" + selected.getId());
            refreshAll();
        } catch (Exception e) {
            actionResultLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) { actionResultLabel.setText("Select a reservation first."); return; }
        Main.getFacade().cancelReservation(selected.getId());
        actionResultLabel.setText("Cancelled reservation #" + selected.getId());
        refreshAll();
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
    }

    @FXML
    private void handleUtilizationReport() {
        reportArea.setText(Main.getFacade().generateUtilizationReport());
    }

    @FXML
    private void handleMaintenanceReport() {
        reportArea.setText(Main.getFacade().generateMaintenanceHistoryReport());
    }

    @FXML
    private void handleLogout() {
        Session.setCurrentUser(null);
        Navigator.clearHistory();
        Navigator.goTo("/fxml/login.fxml", "LabResa - Sign In");
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
