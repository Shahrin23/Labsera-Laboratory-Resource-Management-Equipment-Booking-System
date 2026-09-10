package com.labresa.controller;

import com.labresa.Main;
import com.labresa.Navigator;
import com.labresa.Session;
import com.labresa.model.*;
import com.labresa.patterns.ReservationBuilder;
import com.labresa.patterns.approval.ApprovalResult;
import com.labresa.ui.CartItem;
import com.labresa.ui.DateTimeFormat;
import com.labresa.ui.ResourceIcons;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label currentUserLabel;
    @FXML private TabPane mainTabPane;
    @FXML private Tab bookTab;
    @FXML private Tab approvalTab;
    @FXML private Tab maintenanceTab;

    // ---- Book a Resource ----
    @FXML private VBox bookingStep1;
    @FXML private VBox bookingStep2;
    @FXML private TableView<Resource> resourceTable;
    @FXML private TableColumn<Resource, Resource> colResIcon;
    @FXML private TableColumn<Resource, String> colResName;
    @FXML private TableColumn<Resource, String> colResCategory;
    @FXML private TableColumn<Resource, String> colResStatus;
    @FXML private TableColumn<Resource, String> colResAvailable;

    @FXML private ComboBox<Resource> resourceCombo;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private ListView<CartItem> cartListView;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField notesField;
    @FXML private CheckBox recurringCheck;
    @FXML private VBox formalLetterBox;
    @FXML private TextField supervisorNameField;
    @FXML private ComboBox<String> supervisorRoleCombo;
    @FXML private TextField letterReferenceField;
    @FXML private Label step1ErrorLabel;
    @FXML private VBox confirmItemsBox;
    @FXML private Label requestResultLabel;

    // ---- My Reservations ----
    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> colResvId;
    @FXML private TableColumn<Reservation, String> colResvResource;
    @FXML private TableColumn<Reservation, Integer> colResvQty;
    @FXML private TableColumn<Reservation, String> colResvStart;
    @FXML private TableColumn<Reservation, String> colResvEnd;
    @FXML private TableColumn<Reservation, String> colResvStatus;
    @FXML private Label actionResultLabel;

    // ---- Approval Queue ----
    @FXML private TableView<Reservation> approvalTable;
    @FXML private TableColumn<Reservation, String> colApprId;
    @FXML private TableColumn<Reservation, String> colApprResource;
    @FXML private TableColumn<Reservation, String> colApprApplicant;
    @FXML private TableColumn<Reservation, String> colApprSupervisor;
    @FXML private TableColumn<Reservation, String> colApprLetter;
    @FXML private TableColumn<Reservation, String> colApprDates;
    @FXML private TextField approvalCommentsField;
    @FXML private Label approvalResultLabel;

    // ---- Manage Resources ----
    @FXML private TableView<Resource> maintenanceResourceTable;
    @FXML private TableColumn<Resource, String> colMaintName;
    @FXML private TableColumn<Resource, String> colMaintCategory;
    @FXML private TableColumn<Resource, String> colMaintQty;
    @FXML private TableColumn<Resource, String> colMaintStatus;
    @FXML private TableColumn<Resource, String> colMaintUsage;
    @FXML private TextField maintenanceNotesField;
    @FXML private Label maintenanceResultLabel;

    // ---- Notifications ----
    @FXML private ListView<Notification> notificationList;

    // ---- Reports ----
    @FXML private TableView<Resource> utilizationTable;
    @FXML private TableColumn<Resource, String> colUtilName;
    @FXML private TableColumn<Resource, String> colUtilCategory;
    @FXML private TableColumn<Resource, String> colUtilAvailable;
    @FXML private TableColumn<Resource, String> colUtilUsage;
    @FXML private TableColumn<Resource, String> colUtilStatus;

    @FXML private TableView<MaintenanceRecord> maintenanceHistoryTable;
    @FXML private TableColumn<MaintenanceRecord, String> colMhResource;
    @FXML private TableColumn<MaintenanceRecord, String> colMhReason;
    @FXML private TableColumn<MaintenanceRecord, String> colMhStart;
    @FXML private TableColumn<MaintenanceRecord, String> colMhEnd;

    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();
    private final Map<Integer, Resource> resourceById = new HashMap<>();

    private List<CartItem> pendingCart;
    private LocalDateTime pendingStart;
    private LocalDateTime pendingEnd;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        currentUserLabel.setText(user.getName() + "  \u2022  " + user.getRole());

        // Role-gate tabs
        if (user.getRole() != User.Role.FACULTY && user.getRole() != User.Role.ADMIN) {
            mainTabPane.getTabs().remove(approvalTab);
        }
        if (user.getRole() != User.Role.TECHNICIAN && user.getRole() != User.Role.ADMIN) {
            mainTabPane.getTabs().remove(maintenanceTab);
        }

        setupResourceTable();
        setupBookingControls();
        setupReservationTable();
        setupApprovalTable();
        setupMaintenanceTable();
        setupReportTables();
        setupNotificationList();

        refreshAll();
    }

    private String iconKeyFor(Resource r) {
        return r instanceof Equipment ? ((Equipment) r).getEquipmentKind() : r.getType();
    }

    // ==================== SETUP ====================

    private void setupResourceTable() {
        colResIcon.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colResIcon.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Resource r, boolean empty) {
                super.updateItem(r, empty);
                setGraphic(empty || r == null ? null : ResourceIcons.badge(iconKeyFor(r)));
                setText(null);
            }
        });
        colResName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colResCategory.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().getCategory().toString()));
        colResCategory.setCellFactory(col -> categoryBadgeCell());
        colResStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colResStatus.setCellFactory(col -> statusBadgeCell());
        colResAvailable.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                r.getValue().getAvailableQuantity() + " / " + r.getValue().getTotalQuantity()));
    }

    private void setupBookingControls() {
        resourceCombo.setCellFactory(cb -> resourceListCell());
        resourceCombo.setButtonCell(resourceListCell());
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        supervisorRoleCombo.setItems(FXCollections.observableArrayList("Supervisor", "Chairman", "Director"));

        cartListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label label = new Label(item.toString());
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                Button remove = new Button("Remove");
                remove.getStyleClass().add("btn-link");
                remove.setOnAction(e -> {
                    cart.remove(item);
                    updateFormalLetterVisibility();
                });
                HBox row = new HBox(8, label, spacer, remove);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });
        cartListView.setItems(cart);

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupReservationTable() {
        colResvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colResvResource.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(resourceNameFor(r.getValue().getResourceId())));
        colResvQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colResvStart.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(DateTimeFormat.format(r.getValue().getStartTime())));
        colResvEnd.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(DateTimeFormat.format(r.getValue().getEndTime())));
        colResvStatus.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(String.valueOf(r.getValue().getStatus())));
        colResvStatus.setCellFactory(col -> statusBadgeCell());
    }

    private void setupApprovalTable() {
        colApprId.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(String.valueOf(r.getValue().getId())));
        colApprResource.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(resourceNameFor(r.getValue().getResourceId())));
        colApprApplicant.setCellValueFactory(r -> {
            User applicant = Main.getFacade().findUserById(r.getValue().getUserId());
            return new javafx.beans.property.SimpleStringProperty(applicant == null ? "#" + r.getValue().getUserId() : applicant.getName());
        });
        colApprSupervisor.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                nullToDash(r.getValue().getSupervisorName()) + " (" + nullToDash(r.getValue().getSupervisorRole()) + ")"));
        colApprLetter.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(nullToDash(r.getValue().getLetterReference())));
        colApprDates.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                DateTimeFormat.format(r.getValue().getStartTime()) + "  to  " + DateTimeFormat.format(r.getValue().getEndTime())));
    }

    private void setupMaintenanceTable() {
        colMaintName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMaintCategory.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().getCategory().toString()));
        colMaintCategory.setCellFactory(col -> categoryBadgeCell());
        colMaintQty.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                r.getValue().getTotalQuantity() + " / " + r.getValue().getAvailableQuantity()));
        colMaintStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colMaintStatus.setCellFactory(col -> statusBadgeCell());
        colMaintUsage.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                r.getValue().getUsageCounter() + " / " + r.getValue().getMaintenanceThreshold()));
    }

    private void setupReportTables() {
        colUtilName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUtilCategory.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().getCategory().toString()));
        colUtilCategory.setCellFactory(col -> categoryBadgeCell());
        colUtilAvailable.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                r.getValue().getAvailableQuantity() + " / " + r.getValue().getTotalQuantity()));
        colUtilUsage.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                r.getValue().getUsageCounter() + " / " + r.getValue().getMaintenanceThreshold()));
        colUtilStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colUtilStatus.setCellFactory(col -> statusBadgeCell());

        colMhResource.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(resourceNameFor(r.getValue().getResourceId())));
        colMhReason.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(String.valueOf(r.getValue().getReason())));
        colMhStart.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(DateTimeFormat.format(r.getValue().getStartDate())));
        colMhEnd.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                r.getValue().getEndDate() == null ? "Ongoing" : DateTimeFormat.format(r.getValue().getEndDate())));
    }

    private void setupNotificationList() {
        notificationList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setGraphic(null); return; }
                Label message = new Label(n.getMessage());
                message.setWrapText(true);
                Label time = new Label(DateTimeFormat.format(n.getCreatedAt()) + "   \u2022   " + n.getType());
                time.getStyleClass().add("hint-label");
                VBox box = new VBox(3, message, time);
                setGraphic(box);
            }
        });
    }

    // ==================== SHARED CELL RENDERERS ====================

    private <T> TableCell<T, String> statusBadgeCell() {
        return new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.getStyleClass().addAll("badge", "badge-" + status.toLowerCase());
                setGraphic(badge);
                setText(null);
            }
        };
    }

    private <T> TableCell<T, String> categoryBadgeCell() {
        return new TableCell<>() {
            @Override protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) { setGraphic(null); return; }
                Label badge = new Label(category);
                badge.getStyleClass().addAll("badge", "COMMON".equals(category) ? "badge-common" : "badge-special");
                setGraphic(badge);
                setText(null);
            }
        };
    }

    private ListCell<Resource> resourceListCell() {
        return new ListCell<>() {
            @Override protected void updateItem(Resource r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setGraphic(null); setText(null); return; }
                StackPane icon = ResourceIcons.badge(iconKeyFor(r));
                Label text = new Label(r.getName() + "  (" + r.getAvailableQuantity() + "/" + r.getTotalQuantity() + " available)");
                HBox box = new HBox(8, icon, text);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }
        };
    }

    private String resourceNameFor(int resourceId) {
        Resource r = resourceById.get(resourceId);
        return r == null ? "#" + resourceId : r.getName();
    }

    private String nullToDash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    // ==================== REFRESH ====================

    private void refreshAll() {
        List<Resource> resources = Main.getFacade().getResourceCatalog();
        resourceById.clear();
        for (Resource r : resources) resourceById.put(r.getId(), r);

        resourceTable.setItems(FXCollections.observableArrayList(resources));
        resourceCombo.setItems(FXCollections.observableArrayList(resources));

        List<Reservation> mine = Main.getFacade().getReservationsForUser(Session.getCurrentUser().getId());
        reservationTable.setItems(FXCollections.observableArrayList(mine));

        notificationList.setItems(FXCollections.observableArrayList(Main.getFacade().getNotifications(Session.getCurrentUser().getId())));

        if (mainTabPane.getTabs().contains(approvalTab)) refreshApprovalQueue();
        if (mainTabPane.getTabs().contains(maintenanceTab)) refreshMaintenanceResources();
        refreshReports();
    }

    private void refreshApprovalQueue() {
        approvalTable.setItems(FXCollections.observableArrayList(Main.getFacade().getPendingApprovals()));
    }

    private void refreshMaintenanceResources() {
        maintenanceResourceTable.setItems(FXCollections.observableArrayList(Main.getFacade().getResourceCatalog()));
    }

    private void refreshReports() {
        utilizationTable.setItems(FXCollections.observableArrayList(Main.getFacade().getUtilizationData()));
        maintenanceHistoryTable.setItems(FXCollections.observableArrayList(Main.getFacade().getMaintenanceHistoryData()));
    }

    // ==================== BOOKING CART ====================

    @FXML
    private void handleAddToCart() {
        hideError(step1ErrorLabel);
        Resource resource = resourceCombo.getValue();
        int qty = quantitySpinner.getValue();
        if (resource == null) { showError(step1ErrorLabel, "Please select a resource."); return; }
        if (qty > resource.getAvailableQuantity()) {
            showError(step1ErrorLabel, "Only " + resource.getAvailableQuantity() + " unit(s) of '" + resource.getName() + "' currently show as available.");
            return;
        }
        cart.add(new CartItem(resource, qty));
        updateFormalLetterVisibility();
    }

    private void updateFormalLetterVisibility() {
        boolean needsLetter = cart.stream().anyMatch(i -> i.getResource().getCategory() == Resource.Category.SPECIAL);
        formalLetterBox.setVisible(needsLetter);
        formalLetterBox.setManaged(needsLetter);
    }

    @FXML
    private void handleSaveAndContinue() {
        hideError(step1ErrorLabel);
        if (cart.isEmpty()) { showError(step1ErrorLabel, "Add at least one item to your booking list."); return; }

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) { showError(step1ErrorLabel, "Please choose both a start and end date."); return; }
        if (end.isBefore(start)) { showError(step1ErrorLabel, "End date cannot be before the start date."); return; }
        if (start.isBefore(LocalDate.now())) { showError(step1ErrorLabel, "Start date cannot be in the past."); return; }

        boolean needsLetter = cart.stream().anyMatch(i -> i.getResource().getCategory() == Resource.Category.SPECIAL);
        if (needsLetter && (supervisorNameField.getText().isBlank() || supervisorRoleCombo.getValue() == null || letterReferenceField.getText().isBlank())) {
            showError(step1ErrorLabel, "Special equipment in your list requires supervisor name, role, and a letter reference.");
            return;
        }

        pendingCart = new ArrayList<>(cart);
        pendingStart = start.atStartOfDay();
        pendingEnd = end.atTime(LocalTime.of(23, 59));

        confirmItemsBox.getChildren().clear();
        for (CartItem item : pendingCart) {
            confirmItemsBox.getChildren().add(new Label("\u2022  " + item));
        }
        confirmItemsBox.getChildren().add(new Label("Dates:  " + start + "  \u2192  " + end));
        if (needsLetter) {
            confirmItemsBox.getChildren().add(new Label("Formal letter: " + supervisorNameField.getText() + " (" + supervisorRoleCombo.getValue() + ") - " + letterReferenceField.getText()));
        }
        requestResultLabel.setText("");

        bookingStep1.setVisible(false); bookingStep1.setManaged(false);
        bookingStep2.setVisible(true); bookingStep2.setManaged(true);
    }

    @FXML
    private void handleBackToStep1() {
        bookingStep2.setVisible(false); bookingStep2.setManaged(false);
        bookingStep1.setVisible(true); bookingStep1.setManaged(true);
    }

    @FXML
    private void handleConfirmBooking() {
        StringBuilder summary = new StringBuilder();
        for (CartItem item : pendingCart) {
            try {
                Reservation reservation = new ReservationBuilder(
                        item.getResource().getId(), Session.getCurrentUser().getId(),
                        Session.getCurrentUser().getRole(), pendingStart, pendingEnd)
                        .quantity(item.getQuantity())
                        .recurring(recurringCheck.isSelected())
                        .notes(notesField.getText())
                        .formalLetter(supervisorNameField.getText(), supervisorRoleCombo.getValue(), letterReferenceField.getText())
                        .build();
                ApprovalResult result = Main.getFacade().requestReservation(reservation);
                summary.append(item.getResource().getName()).append(" x").append(item.getQuantity()).append(": ").append(result).append("\n");
            } catch (Exception e) {
                summary.append(item.getResource().getName()).append(": Error - ").append(e.getMessage()).append("\n");
            }
        }
        requestResultLabel.setText(summary.toString());
        cart.clear();
        notesField.clear();
        supervisorNameField.clear();
        letterReferenceField.clear();
        supervisorRoleCombo.setValue(null);
        updateFormalLetterVisibility();
        refreshAll();
    }

    // ==================== MY RESERVATIONS ====================

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
            actionResultLabel.setText("Checked out reservation #" + selected.getId() + ". If returned early, the end time has been updated to reflect that.");
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
        // Genuinely useful: re-pulls live data in case someone else's action (an admin's
        // approval, a technician's maintenance toggle) changed something since this screen loaded.
        refreshAll();
        actionResultLabel.setText("Refreshed.");
    }

    // ==================== APPROVAL QUEUE ====================

    @FXML
    private void handleApprove() { decide(true); }

    @FXML
    private void handleReject() { decide(false); }

    private void decide(boolean approve) {
        Reservation selected = approvalTable.getSelectionModel().getSelectedItem();
        if (selected == null) { approvalResultLabel.setText("Select a request first."); return; }
        try {
            Main.getFacade().decideOnReservation(selected.getId(), Session.getCurrentUser().getId(), approve, approvalCommentsField.getText());
            approvalResultLabel.setText("Reservation #" + selected.getId() + " " + (approve ? "approved." : "rejected."));
            approvalCommentsField.clear();
            refreshAll();
        } catch (Exception e) {
            approvalResultLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshApprovals() {
        refreshApprovalQueue();
    }

    // ==================== MANAGE RESOURCES ====================

    @FXML
    private void handleStartMaintenance() {
        Resource selected = maintenanceResourceTable.getSelectionModel().getSelectedItem();
        if (selected == null) { maintenanceResultLabel.setText("Select a resource first."); return; }
        try {
            Main.getFacade().reportDamage(selected.getId(), Session.getCurrentUser().getId(), maintenanceNotesField.getText());
            maintenanceResultLabel.setText("'" + selected.getName() + "' marked unavailable for maintenance.");
            refreshAll();
        } catch (Exception e) {
            maintenanceResultLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCompleteMaintenance() {
        Resource selected = maintenanceResourceTable.getSelectionModel().getSelectedItem();
        if (selected == null) { maintenanceResultLabel.setText("Select a resource first."); return; }
        try {
            Main.getFacade().completeMaintenance(selected.getId(), maintenanceNotesField.getText());
            maintenanceResultLabel.setText("'" + selected.getName() + "' marked available again.");
            refreshAll();
        } catch (Exception e) {
            maintenanceResultLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshMaintenance() {
        refreshMaintenanceResources();
    }

    // ==================== REPORTS ====================

    @FXML
    private void handleRefreshReports() {
        refreshReports();
    }

    // ==================== LOGOUT ====================

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
