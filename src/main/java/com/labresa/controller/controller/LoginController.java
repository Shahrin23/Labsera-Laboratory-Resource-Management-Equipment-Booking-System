package com.labresa.controller;

import com.labresa.Main;
import com.labresa.Navigator;
import com.labresa.Session;
import com.labresa.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || passwordField.getText().isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        User user = Main.getFacade().login(email, passwordField.getText());
        if (user == null) {
            showError("Invalid email or password.");
            return;
        }
        Session.setCurrentUser(user);
        Navigator.clearHistory();
        Navigator.goTo("/fxml/dashboard.fxml", "LabResa - " + user.getName() + " (" + user.getRole() + ")");
    }

    @FXML
    private void goToSignUp() {
        Navigator.push("/fxml/login.fxml", "/fxml/register.fxml", "LabResa - Sign Up");
    }

    @FXML
    private void goToForgotPassword() {
        Navigator.push("/fxml/login.fxml", "/fxml/forgot_password.fxml", "LabResa - Reset Password");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
