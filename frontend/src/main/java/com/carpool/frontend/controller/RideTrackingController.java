package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.model.Booking;
import com.carpool.frontend.service.RideService;
import com.carpool.frontend.util.SessionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Real-time Ride Tracking Controller
 * Implements Observer Pattern for live updates
 * Follows Single Responsibility Principle
 */
public class RideTrackingController {

    @FXML private Label rideTitleLabel;
    @FXML private Label sourceLabel;
    @FXML private Label destinationLabel;
    @FXML private Label driverLabel;
    @FXML private Label currentLocationLabel;
    @FXML private Label estimatedTimeLabel;
    @FXML private Label statusLabel;
    @FXML private Label passengerCountLabel;
    @FXML private ProgressBar rideProgressBar;
    @FXML private Button startRideButton;
    @FXML private Button completeRideButton;
    @FXML private Button emergencyButton;
    @FXML private Button shareTripButton;
    @FXML private VBox passengerListVBox;
    @FXML private ListView<String> routeListView;

    private Ride activeRide;
    private final RideService rideService = new RideService();
    private Timeline trackingTimeline;
    private boolean isTracking = false;

    @FXML
    public void initialize() {
        loadActiveRide();
        setupRealTimeUpdates();
    }
    
    private void loadActiveRide() {
        new Thread(() -> {
            try {
                List<Ride> myRides = rideService.getMyRides();
                activeRide = myRides.stream()
                    .filter(r -> r.getStatus() == RideStatus.PUBLISHED || r.getStatus() == RideStatus.IN_PROGRESS)
                    .findFirst()
                    .orElse(null);
                
                Platform.runLater(this::updateUI);
            } catch (Exception e) {
                System.err.println("Error loading active ride: " + e.getMessage());
            }
        }).start();
    }
    
    private void setupRealTimeUpdates() {
        // Update every 30 seconds for demo purposes
        trackingTimeline = new Timeline(
            new KeyFrame(Duration.seconds(30), event -> updateRideStatus())
        );
        trackingTimeline.setCycleCount(Animation.INDEFINITE);
    }
    
    private void updateUI() {
        if (activeRide == null) {
            rideTitleLabel.setText("No Active Ride");
            startRideButton.setDisable(true);
            completeRideButton.setDisable(true);
            return;
        }
        
        rideTitleLabel.setText("Ride: " + activeRide.getSource() + " to " + activeRide.getDestination());
        sourceLabel.setText("From: " + activeRide.getSource());
        destinationLabel.setText("To: " + activeRide.getDestination());
        driverLabel.setText("Driver: " + activeRide.getDriver().getName());
        statusLabel.setText("Status: " + activeRide.getStatus());
        passengerCountLabel.setText("Passengers: " + getPassengerCount() + "/" + activeRide.getTotalSeats());
        
        // Update buttons based on status
        switch (activeRide.getStatus()) {
            case PUBLISHED:
                startRideButton.setDisable(false);
                completeRideButton.setDisable(true);
                rideProgressBar.setProgress(0.0);
                break;
            case IN_PROGRESS:
                startRideButton.setDisable(true);
                completeRideButton.setDisable(false);
                updateProgress();
                break;
            case COMPLETED:
                startRideButton.setDisable(true);
                completeRideButton.setDisable(true);
                rideProgressBar.setProgress(1.0);
                if (trackingTimeline != null) {
                    trackingTimeline.stop();
                }
                break;
            default:
                startRideButton.setDisable(true);
                completeRideButton.setDisable(true);
        }
        
        loadPassengerList();
        loadRouteDetails();
    }
    
    private void updateRideStatus() {
        if (activeRide == null || !isTracking) return;
        
        new Thread(() -> {
            try {
                // Refresh ride data
                activeRide = rideService.getRideById(activeRide.getId());
                
                Platform.runLater(() -> {
                    updateUI();
                    if (activeRide.getStatus() == RideStatus.IN_PROGRESS) {
                        updateProgress();
                        updateCurrentLocation();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error updating ride status: " + e.getMessage());
            }
        }).start();
    }
    
    private void updateProgress() {
        // Simulate progress based on time elapsed
        if (activeRide != null && activeRide.getDepartureTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime departure = activeRide.getDepartureTime();
            
            // Estimate 30 minutes total ride time for demo
            long elapsedMinutes = java.time.Duration.between(departure, now).toMinutes();
            double progress = Math.min(1.0, elapsedMinutes / 30.0);
            
            rideProgressBar.setProgress(progress);
        }
    }
    
    private void updateCurrentLocation() {
        // Simulate current location updates
        if (activeRide != null && activeRide.getStatus() == RideStatus.IN_PROGRESS) {
            String[] waypoints = {"Main Gate", "Library", "Computer Science Dept", "Engineering Block", "Destination"};
            double progress = rideProgressBar.getProgress();
            int waypointIndex = (int) (progress * (waypoints.length - 1));
            
            currentLocationLabel.setText("Current Location: " + waypoints[Math.min(waypointIndex, waypoints.length - 1)]);
            
            // Update estimated time
            long remainingMinutes = Math.max(0, (long) ((1.0 - progress) * 30));
            estimatedTimeLabel.setText("ETA: " + remainingMinutes + " minutes");
        }
    }
    
    private int getPassengerCount() {
        if (activeRide == null) return 0;
        return activeRide.getTotalSeats() - activeRide.getAvailableSeats();
    }
    
    private void loadPassengerList() {
        if (activeRide == null) return;
        
        new Thread(() -> {
            try {
                List<Booking> bookings = rideService.getRideBookings(activeRide.getId());
                
                Platform.runLater(() -> {
                    passengerListVBox.getChildren().clear();
                    bookings.forEach(booking -> {
                        Label passengerLabel = new Label(
                            booking.getPassenger() + " (" + booking.getSeatsBooked() + " seats)"
                        );
                        passengerListVBox.getChildren().add(passengerLabel);
                    });
                });
            } catch (Exception e) {
                System.err.println("Error loading passenger list: " + e.getMessage());
            }
        }).start();
    }
    
    private void loadRouteDetails() {
        if (activeRide == null) return;
        
        // Simulate route waypoints
        routeListView.getItems().clear();
        routeListView.getItems().addAll(
            "Start: " + activeRide.getSource(),
            "Waypoint 1: Library",
            "Waypoint 2: Computer Science Dept", 
            "End: " + activeRide.getDestination()
        );
    }

    @FXML
    private void startRide() {
        if (activeRide == null) return;
        
        new Thread(() -> {
            try {
                rideService.startRide(activeRide.getId());
                
                Platform.runLater(() -> {
                    activeRide.setStatus(RideStatus.IN_PROGRESS);
                    updateUI();
                    isTracking = true;
                    trackingTimeline.play();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Error starting ride: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void completeRide() {
        if (activeRide == null) return;
        
        new Thread(() -> {
            try {
                rideService.completeRide(activeRide.getId());
                
                Platform.runLater(() -> {
                    activeRide.setStatus(RideStatus.COMPLETED);
                    updateUI();
                    isTracking = false;
                    trackingTimeline.stop();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Error completing ride: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void triggerEmergency() {
        try {
            // Navigate to emergency screen
            App.setRoot("emergency_alert");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void shareTripDetails() {
        if (activeRide == null) return;
        
        new Thread(() -> {
            try {
                rideService.shareTripDetails(activeRide.getId());
                
                Platform.runLater(() -> {
                    // Show confirmation
                    System.out.println("Trip details shared with emergency contacts");
                });
            } catch (Exception e) {
                System.err.println("Error sharing trip details: " + e.getMessage());
            }
        }).start();
    }
    
    @FXML
    private void goBack() throws IOException {
        if (trackingTimeline != null) {
            trackingTimeline.stop();
        }
        App.setRoot("dashboard");
    }
    
    @FXML
    private void refreshStatus() {
        updateRideStatus();
    }
}
