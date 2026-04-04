package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.IOException;

public class AdminDashboardController {

    @FXML private Label adminWelcomeLabel;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            adminWelcomeLabel.setText("Admin Console [Logged in as: " + SessionManager.getCurrentUser().getName() + "]");
        }
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
