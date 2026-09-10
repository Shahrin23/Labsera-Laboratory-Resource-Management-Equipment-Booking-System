package com.labresa.controller;

import com.labresa.Main;
import com.labresa.Navigator;
import com.labresa.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public class RegisterController {

    @FXML private ScrollPane formScrollPane;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField sessionField;
    @FXML private TextField classRollField;
    @FXML private TextField emailField;
    @FXML private Label emailStatusLabel;
    @FXML private ComboBox<User.Role> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordStatusLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label confirmStatusLabel;
    @FXML private Label formErrorLabel;
    @FXML private Button signUpButton;

    @FXML private StackPane successOverlay;
    @FXML private Pane successAnimationPane;

    private boolean emailAvailable = false;
    private PauseTransition emailCheckDebounce;

    @FXML
    public void initialize() {
        roleCombo.getItems().setAll(User.Role.UNDERGRAD, User.Role.GRAD, User.Role.FACULTY, User.Role.TECHNICIAN);
        roleCombo.getSelectionModel().select(User.Role.UNDERGRAD);

        emailCheckDebounce = new PauseTransition(Duration.millis(400));
        emailCheckDebounce.setOnFinished(e -> checkEmailAvailability());

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            emailAvailable = false;
            hideLabel(emailStatusLabel);
            emailCheckDebounce.stop();
            if (isValidEmailFormat(newVal.trim())) {
                emailCheckDebounce.playFromStart();
            }
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePassword());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateConfirmPassword());
    }

    private boolean isValidEmailFormat(String email) {
        return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private void checkEmailAvailability() {
        String email = emailField.getText().trim();
        CompletableFuture
                .supplyAsync(() -> Main.getFacade().emailExists(email))
                .thenAccept(exists -> Platform.runLater(() -> {
                    emailAvailable = !exists;
                    if (exists) {
                        showLabel(emailStatusLabel, "This email is already registered.", "error-label");
                    } else {
                        showLabel(emailStatusLabel, "Email is available.", "success-label");
                    }
                }));
    }

    private void validatePassword() {
        String pwd = passwordField.getText();
        if (pwd.isEmpty()) {
            hideLabel(passwordStatusLabel);
        } else if (pwd.length() < 8) {
            showLabel(passwordStatusLabel, "Password must be at least 8 characters.", "error-label");
        } else {
            showLabel(passwordStatusLabel, "Password strength OK.", "success-label");
        }
        validateConfirmPassword();
    }

    private void validateConfirmPassword() {
        String confirm = confirmPasswordField.getText();
        if (confirm.isEmpty()) {
            hideLabel(confirmStatusLabel);
        } else if (!confirm.equals(passwordField.getText())) {
            showLabel(confirmStatusLabel, "Passwords do not match.", "error-label");
        } else {
            showLabel(confirmStatusLabel, "Passwords match.", "success-label");
        }
    }

    @FXML
    private void handleSignUp() {
        hideLabel(formErrorLabel);

        if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
            showFormError("Please enter your first and last name.");
            return;
        }
        if (classRollField.getText().isBlank()) {
            showFormError("Please enter your class roll.");
            return;
        }
        if (!isValidEmailFormat(emailField.getText().trim())) {
            showFormError("Please enter a valid email address.");
            return;
        }
        if (!emailAvailable) {
            showFormError("Please use an email that isn't already registered.");
            return;
        }
        if (passwordField.getText().length() < 8) {
            showFormError("Password must be at least 8 characters.");
            return;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showFormError("Passwords do not match.");
            return;
        }

        try {
            Main.getFacade().register(firstNameField.getText(), lastNameField.getText(),
                    sessionField.getText(), classRollField.getText(), emailField.getText().trim(),
                    passwordField.getText(), roleCombo.getValue());
            playSuccessAnimation();
        } catch (IllegalArgumentException e) {
            showFormError(e.getMessage());
        }
    }

    /** Draws a circle progressively around a checkmark, then navigates to Login. */
    private void playSuccessAnimation() {
        formScrollPane.setVisible(false);
        formScrollPane.setManaged(false);
        successOverlay.setVisible(true);
        successOverlay.setManaged(true);

        double size = 140;
        double radius = 55;
        double center = size / 2;

        Arc arc = new Arc(center, center, radius, radius, 90, 0);
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.web("#3B5BFF"));
        arc.setStrokeWidth(8);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);

        javafx.scene.text.Text check = new javafx.scene.text.Text(center - 16, center + 10, "\u2713");
        check.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        check.setFill(Color.web("#3B5BFF"));
        check.setOpacity(0);

        successAnimationPane.getChildren().setAll(arc, check);

        Timeline drawCircle = new Timeline(
                new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(arc.lengthProperty(), 0)),
                new KeyFrame(Duration.millis(900), new javafx.animation.KeyValue(arc.lengthProperty(), -360))
        );
        drawCircle.setOnFinished(e -> {
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.millis(300), check);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
        drawCircle.play();

        PauseTransition wait = new PauseTransition(Duration.seconds(2.6));
        wait.setOnFinished(e -> Navigator.goTo("/fxml/login.fxml", "LabResa - Sign In"));
        wait.play();
    }

    @FXML
    private void goBack() {
        Navigator.back("LabResa - Sign In");
    }

    private void showLabel(Label label, String text, String styleClass) {
        label.getStyleClass().removeAll("error-label", "success-label");
        label.getStyleClass().add(styleClass);
        label.setText(text);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideLabel(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    private void showFormError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setVisible(true);
        formErrorLabel.setManaged(true);
    }
}
