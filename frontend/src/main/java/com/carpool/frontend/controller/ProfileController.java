package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.service.AuthService;
import com.carpool.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;

    private AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            nameField.setText(SessionManager.getCurrentUser().getName());
            emailField.setText(SessionManager.getCurrentUser().getEmail());
            phoneField.setText(SessionManager.getCurrentUser().getPhone());
            roleLabel.setText(SessionManager.getCurrentUser().getRole());
            
            emailField.setDisable(true); // Cannot edit email
        }
    }

    @FXML
    private void handleSaveChanges() {
        String newName = nameField.getText();
        String newPhone = phoneField.getText();

        if (newName.trim().isEmpty() || newPhone.trim().isEmpty()) {
            statusLabel.setText("Name and Phone cannot be empty!");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (!newPhone.matches("^[0-9]{10}$")) {
            statusLabel.setText("Phone must be exactly 10 digits.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            phoneField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            return;
        } else phoneField.setStyle("");

        try {
            Long userId = SessionManager.getCurrentUser().getUserId();
            boolean success = authService.updateProfile(userId, newName, newPhone);
            if (success) {
                // Update local session
                SessionManager.getCurrentUser().setName(newName);
                SessionManager.getCurrentUser().setPhone(newPhone);
                statusLabel.setText("Profile Updated successfully!");
                statusLabel.setStyle("-fx-text-fill: #10b981;");
            } else {
                statusLabel.setText("Failed to update profile.");
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }
}
