package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.model.User;
import com.carpool.frontend.service.RideService;
import com.carpool.frontend.service.AuthService;
import com.carpool.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.io.IOException;
import java.util.List;

/**
 * Enhanced Dashboard Controller with ride tracking and analytics
 * Implements MVC pattern and follows Single Responsibility Principle
 */
public class EnhancedDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label activeRidesLabel;
    @FXML private Label totalEarningsLabel;
    @FXML private Label averageRatingLabel;
    @FXML private VBox riderMenu;
    @FXML private VBox driverMenu;
    @FXML private Button driverActionButton;
    @FXML private Button trackRideButton;
    @FXML private Button notificationsButton;
    @FXML private Button analyticsButton;
    @FXML private ProgressBar rideProgress;
    @FXML private HBox statsContainer;
    @FXML private LineChart<String, Number> rideChart;

    private boolean hasActiveRide = false;
    private final RideService rideService = new RideService();
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            com.carpool.frontend.model.AuthResponse currentUser = SessionManager.getCurrentUser();
            welcomeLabel.setText("Welcome, " + currentUser.getName() + "!");
            userRoleLabel.setText("Role: " + currentUser.getRole());
            
            String role = currentUser.getRole();
            
            if ("DRIVER".equalsIgnoreCase(role)) {
                setupDriverView();
            } else {
                setupRiderView();
            }
            
            loadUserStatistics();
            setupRideChart();
        }
    }
    
    private void setupDriverView() {
        driverMenu.setVisible(true);
        driverMenu.setManaged(true);
        riderMenu.setVisible(false);
        riderMenu.setManaged(false);
        
        checkActiveRide();
        loadDriverStatistics();
    }
    
    private void setupRiderView() {
        riderMenu.setVisible(true);
        riderMenu.setManaged(true);
        driverMenu.setVisible(false);
        driverMenu.setManaged(false);
        
        trackRideButton.setVisible(false);
        trackRideButton.setManaged(false);
        
        loadRiderStatistics();
    }
    
    private void loadUserStatistics() {
        new Thread(() -> {
            try {
                User user = authService.getUserProfile(SessionManager.getCurrentUser().getId());
                
                Platform.runLater(() -> {
                    averageRatingLabel.setText(String.format("Rating: %.1f/5.0", user.getRating()));
                });
            } catch (Exception e) {
                System.err.println("Error loading user statistics: " + e.getMessage());
            }
        }).start();
    }
    
    private void loadDriverStatistics() {
        new Thread(() -> {
            try {
                List<Ride> myRides = rideService.getMyRides();
                long completedRides = myRides.stream()
                    .filter(r -> r.getStatus() == RideStatus.COMPLETED)
                    .count();
                
                double totalEarnings = myRides.stream()
                    .filter(r -> r.getStatus() == RideStatus.COMPLETED)
                    .mapToDouble(r -> r.getFarePerSeat() * r.getTotalSeats())
                    .sum();
                
                Platform.runLater(() -> {
                    activeRidesLabel.setText("Active Rides: " + 
                        myRides.stream().filter(r -> r.getStatus() == RideStatus.IN_PROGRESS).count());
                    totalEarningsLabel.setText(String.format("Total Earnings: $%.2f", totalEarnings));
                });
            } catch (Exception e) {
                System.err.println("Error loading driver statistics: " + e.getMessage());
            }
        }).start();
    }
    
    private void loadRiderStatistics() {
        new Thread(() -> {
            try {
                List<Ride> bookedRides = rideService.getBookedRides();
                long completedRides = bookedRides.stream()
                    .filter(r -> r.getStatus() == RideStatus.COMPLETED)
                    .count();
                
                Platform.runLater(() -> {
                    activeRidesLabel.setText("Booked Rides: " + 
                        bookedRides.stream().filter(r -> r.getStatus() == RideStatus.IN_PROGRESS).count());
                    totalEarningsLabel.setText("Completed Rides: " + completedRides);
                });
            } catch (Exception e) {
                System.err.println("Error loading rider statistics: " + e.getMessage());
            }
        }).start();
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
                        trackRideButton.setDisable(false);
                        updateRideProgress(myRides);
                    } else {
                        driverActionButton.setText("Create a Ride");
                        trackRideButton.setDisable(true);
                        rideProgress.setProgress(0.0);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    driverActionButton.setDisable(false);
                    driverActionButton.setText("Create a Ride");
                    trackRideButton.setDisable(true);
                });
            }
        }).start();
    }
    
    private void updateRideProgress(List<Ride> rides) {
        Ride activeRide = rides.stream()
            .filter(r -> r.getStatus() == RideStatus.IN_PROGRESS)
            .findFirst()
            .orElse(null);
            
        if (activeRide != null) {
            // Simulate progress (in real app, this would come from tracking service)
            double progress = Math.random() * 0.8 + 0.1; // 10-90% progress
            rideProgress.setProgress(progress);
        }
    }
    
    private void setupRideChart() {
        // Setup monthly ride statistics chart
        rideChart.setTitle("Monthly Ride Statistics");
        rideChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Rides");
        
        // Add sample data (in real app, this would come from analytics service)
        series.getData().addAll(
            new XYChart.Data<>("Jan", 4),
            new XYChart.Data<>("Feb", 7),
            new XYChart.Data<>("Mar", 12),
            new XYChart.Data<>("Apr", 8),
            new XYChart.Data<>("May", 15),
            new XYChart.Data<>("Jun", 10)
        );
        
        rideChart.getData().add(series);
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
    private void trackActiveRide() throws IOException {
        App.setRoot("ride_tracking");
    }
    
    @FXML
    private void viewNotifications() throws IOException {
        App.setRoot("notifications");
    }
    
    @FXML
    private void viewAnalytics() throws IOException {
        App.setRoot("analytics");
    }
    
    @FXML
    private void viewRideHistory() throws IOException {
        App.setRoot("ride_history");
    }
    
    @FXML
    private void viewEmergencyContacts() throws IOException {
        App.setRoot("emergency_contacts");
    }
    
    @FXML
    private void viewPreferences() throws IOException {
        App.setRoot("preferences");
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
