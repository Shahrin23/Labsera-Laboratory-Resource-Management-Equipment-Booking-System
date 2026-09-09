package com.labresa.controller;

import com.labresa.Main;
import com.labresa.Navigator;
import com.labresa.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Label emailStatusLabel;
    @FXML private ComboBox<User.Role> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordStatusLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label confirmStatusLabel;
    @FXML private Label formErrorLabel;
    @FXML private Button signUpButton;

    private boolean emailAvailable = false;
    private javafx.animation.PauseTransition emailCheckDebounce;

    @FXML
    public void initialize() {
        roleCombo.getItems().setAll(User.Role.values());
        roleCombo.getSelectionModel().select(User.Role.UNDERGRAD);

        emailCheckDebounce = new javafx.animation.PauseTransition(Duration.millis(400));
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
        // Real DB check runs off the UI thread so typing never feels laggy.
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

        if (nameField.getText().isBlank()) {
            showFormError("Please enter your full name.");
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
            Main.getFacade().register(nameField.getText(), emailField.getText().trim(),
                    passwordField.getText(), roleCombo.getValue());
            Navigator.goTo("/fxml/login.fxml", "LabResa - Sign In");
        } catch (IllegalArgumentException e) {
            showFormError(e.getMessage());
        }
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
