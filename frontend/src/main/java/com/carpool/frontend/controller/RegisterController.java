package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.AuthResponse;
import com.carpool.frontend.service.AuthService;
import com.carpool.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordBox;
    @FXML private Label strengthLabel;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Bind text fields
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        
        // Show/Hide password toggle
        showPasswordBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordTextField.setVisible(true);
                passwordField.setVisible(false);
            } else {
                passwordTextField.setVisible(false);
                passwordField.setVisible(true);
            }
        });

        // Password strength checker dynamically updating
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordStrength(newVal);
        });
    }

    private void checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            strengthLabel.setText("");
            return;
        }
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*].*")) score++;

        if (score <= 1) {
            strengthLabel.setText("Weak");
            strengthLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else if (score == 2 || score == 3) {
            strengthLabel.setText("Medium");
            strengthLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            strengthLabel.setText("Strong");
            strengthLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 11px;");
        }
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String phone = phoneField.getText();
        String role = roleComboBox.getValue();

        if (name.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty() || phone.trim().isEmpty() || role == null) {
            errorLabel.setText("Please fill all fields.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Invalid email format.");
            emailField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            return;
        } else emailField.setStyle("");

        if (!phone.matches("^[0-9]{10}$")) {
            errorLabel.setText("Phone must be exactly 10 digits.");
            phoneField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            return;
        } else phoneField.setStyle("");

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*].*")) score++;

        if (score < 4) {
            errorLabel.setText("Password must be Strong: Use capitals, digits, and symbols.");
            passwordField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            return;
        } else passwordField.setStyle("");

        try {
            AuthResponse response = authService.register(name, email, password, phone, role);
            if (response != null && response.getToken() != null) {
                SessionManager.setCurrentUser(response);
                if ("ADMIN".equals(response.getRole())) {
                    App.setRoot("admin_dashboard");
                } else {
                    App.setRoot("dashboard");
                }
            } else {
                errorLabel.setText("Registration failed. Please try again.");
            }
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() throws IOException {
        App.setRoot("login");
    }
}
