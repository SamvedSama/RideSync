package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.service.RideService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import com.carpool.frontend.service.BookingService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManageRideController {

    @FXML private Label routeLabel;
    @FXML private Label timeLabel;
    @FXML private Label seatsLabel;
    @FXML private Label fareLabel;
    @FXML private Label statusLabel;
    @FXML private Label actionStatusLabel;
    @FXML private HBox startRideBox;
    @FXML private TextField otpField;
    @FXML private Button completeButton;
    
    private Ride currentRide;
    private final RideService rideService = new RideService();
    private final BookingService bookingService = new BookingService();

    @FXML
    public void initialize() {
        actionStatusLabel.setText("Loading active ride...");
        actionStatusLabel.setStyle("-fx-text-fill: #f59e0b;");
        
        new Thread(() -> {
            try {
                List<Ride> myRides = rideService.getMyRides();
                Ride activeRide = myRides.stream()
                        .filter(r -> r.getStatus() == RideStatus.PUBLISHED || 
                                     r.getStatus() == RideStatus.BOOKED || 
                                     r.getStatus() == RideStatus.IN_PROGRESS)
                        .findFirst().orElse(null);

                Platform.runLater(() -> {
                    if (activeRide != null) {
                        currentRide = activeRide;
                        routeLabel.setText(activeRide.getSource() + " -> " + activeRide.getDestination());
                        timeLabel.setText("Departure: " + (activeRide.getDepartureTime() != null ? activeRide.getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A"));
                        int bookedSeats = activeRide.getTotalSeats() - activeRide.getAvailableSeats();
                        seatsLabel.setText("Seats: " + bookedSeats + " Booked | " + activeRide.getAvailableSeats() + " Left | " + activeRide.getTotalSeats() + " Total");
                        fareLabel.setText("Fare per seat: ₹" + String.format("%.2f", activeRide.getFarePerSeat()));
                        statusLabel.setText("Status: " + activeRide.getStatus());
                        actionStatusLabel.setText("");
                        
                        if (activeRide.getStatus() == RideStatus.BOOKED) {
                            startRideBox.setVisible(true);
                            completeButton.setVisible(false);
                        } else if (activeRide.getStatus() == RideStatus.IN_PROGRESS) {
                            startRideBox.setVisible(false);
                            completeButton.setVisible(true);
                        } else {
                            startRideBox.setVisible(false);
                            completeButton.setVisible(false);
                        }
                    } else {
                        actionStatusLabel.setText("No active ride found.");
                        actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    actionStatusLabel.setText("Error loading ride: " + e.getMessage());
                    actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                });
            }
        }).start();
    }

    @FXML
    private void handleStartRide() {
        if (currentRide == null) return;
        String otp = otpField.getText().trim();
        if (otp.isEmpty()) {
            actionStatusLabel.setText("Enter the Start OTP");
            actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }
        
        actionStatusLabel.setText("Starting ride...");
        actionStatusLabel.setStyle("-fx-text-fill: #3b82f6;");
        
        new Thread(() -> {
            try {
                bookingService.startRideWithOtp(currentRide.getId(), otp);
                Platform.runLater(() -> {
                    actionStatusLabel.setText("Ride Started!");
                    actionStatusLabel.setStyle("-fx-text-fill: #10b981;");
                    try {
                        App.setRoot("map_view");
                    } catch (IOException e) { e.printStackTrace(); }
                });
            } catch (Exception e) {
                 Platform.runLater(() -> {
                     actionStatusLabel.setText("Error: " + e.getMessage());
                     actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                 });
            }
        }).start();
    }

    @FXML
    private void handleCompleteRide() {
        updateRideStatus(RideStatus.COMPLETED);
    }

    @FXML
    private void handleCancelRide() {
        updateRideStatus(RideStatus.CANCELLED);
    }

    private void updateRideStatus(RideStatus status) {
        if (currentRide == null) return;
        
        actionStatusLabel.setText("Updating...");
        actionStatusLabel.setStyle("-fx-text-fill: #f59e0b;");
        
        new Thread(() -> {
            try {
                rideService.updateRideStatus(currentRide.getId(), status);
                Platform.runLater(() -> {
                    actionStatusLabel.setText("Ride " + status + " successfully!");
                    actionStatusLabel.setStyle("-fx-text-fill: #10b981;");
                    try {
                        Thread.sleep(1000);
                        goToDashboard();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                 Platform.runLater(() -> {
                     actionStatusLabel.setText("Error: " + e.getMessage());
                     actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                 });
            }
        }).start();
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }
}
