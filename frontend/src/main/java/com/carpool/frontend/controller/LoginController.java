package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.AuthResponse;
import com.carpool.frontend.service.AuthService;
import com.carpool.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordBox;
    @FXML private Label errorLabel;

    private AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Bind fields
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        
        // Listener
        showPasswordBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordTextField.setVisible(true);
                passwordField.setVisible(false);
            } else {
                passwordTextField.setVisible(false);
                passwordField.setVisible(true);
            }
        });
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter email and password.");
            return;
        }

        try {
            AuthResponse response = authService.login(email, password);
            if (response != null && response.getToken() != null) {
                SessionManager.setCurrentUser(response);
                if ("ADMIN".equals(response.getRole())) {
                    App.setRoot("admin_dashboard");
                } else {
                    App.setRoot("dashboard");
                }
            } else {
                errorLabel.setText("Login failed. Check your credentials.");
            }
        } catch (Exception e) {
            errorLabel.setText("Login Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToRegister() throws IOException {
        App.setRoot("register");
    }
}
