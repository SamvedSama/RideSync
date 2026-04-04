package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.service.RideService;
import com.carpool.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private VBox riderMenu;
    @FXML private VBox driverMenu;
    @FXML private Button driverActionButton;

    private boolean hasActiveRide = false;
    private final RideService rideService = new RideService();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUser().getName() + "!");
            String role = SessionManager.getCurrentUser().getRole();
            
            if ("DRIVER".equalsIgnoreCase(role)) {
                driverMenu.setVisible(true);
                driverMenu.setManaged(true);
                riderMenu.setVisible(false);
                riderMenu.setManaged(false);
                
                checkActiveRide();
            } else {
                riderMenu.setVisible(true);
                riderMenu.setManaged(true);
                driverMenu.setVisible(false);
                driverMenu.setManaged(false);
            }
        }
    }

    private void checkActiveRide() {
        driverActionButton.setDisable(true);
        driverActionButton.setText("Checking status...");
        
        new Thread(() -> {
            try {
                List<Ride> myRides = rideService.getMyRides();
                hasActiveRide = myRides.stream().anyMatch(r -> 
                        r.getStatus() == RideStatus.PUBLISHED || 
                        r.getStatus() == RideStatus.BOOKED || 
                        r.getStatus() == RideStatus.IN_PROGRESS);
                        
                Platform.runLater(() -> {
                    driverActionButton.setDisable(false);
                    if (hasActiveRide) {
                        driverActionButton.setText("Manage Active Ride");
                        // We will repurpose goToCreateRide event via check
                    } else {
                        driverActionButton.setText("Create a Ride");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    driverActionButton.setDisable(false);
                    driverActionButton.setText("Create a Ride"); // fallback
                });
            }
        }).start();
    }

    @FXML
    private void goToProfile() throws IOException {
        App.setRoot("profile");
    }
    
    @FXML
    private void goToSearchRides() throws IOException {
        App.setRoot("search_rides");
    }

    @FXML
    private void goToCreateRide() throws IOException {
        if (hasActiveRide) {
            App.setRoot("manage_ride");
        } else {
            App.setRoot("create_ride");
        }
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
