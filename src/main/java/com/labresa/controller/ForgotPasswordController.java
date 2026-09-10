package com.labresa.controller;

import com.labresa.Main;
import com.labresa.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ForgotPasswordController {

    @FXML private Label stepSubtitle;
    @FXML private VBox step1Box;
    @FXML private VBox step2Box;

    @FXML private TextField emailField;
    @FXML private Label step1ErrorLabel;

    @FXML private PasswordField newPasswordField;
    @FXML private Label passwordStatusLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label step2ErrorLabel;

    private String verifiedEmail;

    @FXML
    public void initialize() {
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                hide(passwordStatusLabel);
            } else if (newVal.length() < 8) {
                show(passwordStatusLabel, "Password must be at least 8 characters.", "error-label");
            } else {
                show(passwordStatusLabel, "Password strength OK.", "success-label");
            }
        });
    }

    @FXML
    private void handleVerifyEmail() {
        String email = emailField.getText().trim();
        if (!Main.getFacade().emailExists(email)) {
            showError(step1ErrorLabel, "No account found with that email.");
            return;
        }
        verifiedEmail = email;
        step1Box.setVisible(false);
        step1Box.setManaged(false);
        step2Box.setVisible(true);
        step2Box.setManaged(true);
        stepSubtitle.setText("Step 2 of 2 - choose a new password");
    }

    @FXML
    private void handleResetPassword() {
        if (newPasswordField.getText().length() < 8) {
            showError(step2ErrorLabel, "Password must be at least 8 characters.");
            return;
        }
        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showError(step2ErrorLabel, "Passwords do not match.");
            return;
        }
        try {
            Main.getFacade().resetPassword(verifiedEmail, newPasswordField.getText());
            Navigator.goTo("/fxml/login.fxml", "LabResa - Sign In");
        } catch (IllegalArgumentException e) {
            showError(step2ErrorLabel, e.getMessage());
        }
    }

    @FXML
    private void backToStep1() {
        step2Box.setVisible(false);
        step2Box.setManaged(false);
        step1Box.setVisible(true);
        step1Box.setManaged(true);
        stepSubtitle.setText("Step 1 of 2 - verify your email");
    }

    @FXML
    private void goBack() {
        Navigator.back("LabResa - Sign In");
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void show(Label label, String text, String styleClass) {
        label.getStyleClass().removeAll("error-label", "success-label");
        label.getStyleClass().add(styleClass);
        label.setText(text);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hide(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
