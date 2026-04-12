package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.service.RideService;
import com.carpool.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import com.carpool.frontend.service.BookingService;
import com.carpool.frontend.model.Booking;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
    @FXML private Button ongoingButton;
    @FXML private Button completedButton;
    @FXML private Button cancelledButton;
    @FXML private ListView<Booking> bookingsListView;
    
    private Ride currentRide;
    private final RideService rideService = new RideService();
    private final BookingService bookingService = new BookingService();

    @FXML
    public void initialize() {
        actionStatusLabel.setText("Loading active ride...");
        actionStatusLabel.setStyle("-fx-text-fill: #f59e0b;");
        
        // Initialize empty bookings list
        bookingsListView.getItems().clear();
        bookingsListView.setPlaceholder(new javafx.scene.control.Label("No bookings found. Users need to book your ride first."));
        
        // Setup bookings list view with enhanced styling
        bookingsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Booking booking, boolean empty) {
                super.updateItem(booking, empty);
                if (empty || booking == null) {
                    setGraphic(null);
                } else {
                    // Create custom booking card layout with reduced padding
                    VBox bookingCard = new VBox(8);
                    bookingCard.setStyle("-fx-background-color: linear-gradient(to right, #1e293b, #0f172a); " +
                                       "-fx-background-radius: 12px; -fx-border-color: #334155; -fx-border-width: 1px; " +
                                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3); " +
                                       "-fx-padding: 12px; -fx-margin: 6px 0;");
                    
                    // Passenger name header - reduced font size
                    Label passengerNameLabel = new Label();
                    String passengerName = booking.getRiderName() != null ? booking.getRiderName() : "Unknown Passenger";
                    passengerNameLabel.setText(passengerName);
                    passengerNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #38bdf8; " +
                                              "-fx-padding: 0 0 6px 0; -fx-border-width: 0 0 1px 0; -fx-border-color: #334155;");
                    
                    // Booking details container with better spacing
                    VBox detailsBox = new VBox(6);
                    
                    // Seats info - reduced font size
                    Label seatsLabel = new Label();
                    seatsLabel.setText("Seats: " + booking.getSeatsBooked());
                    seatsLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #10b981; " +
                                      "-fx-background-color: rgba(16, 185, 129, 0.15); -fx-background-radius: 6px; " +
                                      "-fx-padding: 4px 8px; -fx-border-radius: 6px; -fx-border-color: rgba(16, 185, 129, 0.4); " +
                                      "-fx-effect: dropshadow(three-pass-box, rgba(16, 185, 129, 0.2), 3, 0, 0, 1);");
                    
                    // Booking time - reduced font size
                    Label timeLabel = new Label();
                    String time = booking.getOtpGeneratedAt() != null ? 
                        booking.getOtpGeneratedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) : "Booking time not available";
                    timeLabel.setText("Booked: " + time);
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 3px 0;");
                    
                    // OTP info - reduced font size
                    Label otpLabel = new Label();
                    String otp = booking.getStartOtp() != null ? booking.getStartOtp() : "Not generated";
                    otpLabel.setText("OTP: " + otp);
                    otpLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 500; -fx-text-fill: #f59e0b; " +
                                    "-fx-background-color: rgba(245, 158, 11, 0.15); -fx-background-radius: 6px; " +
                                    "-fx-padding: 4px 8px; -fx-border-radius: 6px; -fx-border-color: rgba(245, 158, 11, 0.4); " +
                                    "-fx-effect: dropshadow(three-pass-box, rgba(245, 158, 11, 0.2), 3, 0, 0, 1);");
                    
                    // Status indicator - reduced font size
                    Label statusLabel = new Label();
                    String status = booking.getStatus() != null ? booking.getStatus().toString() : "UNKNOWN";
                    statusLabel.setText("Status: " + status);
                    statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 500; -fx-text-fill: #e2e8f0; " +
                                      "-fx-background-color: rgba(148, 163, 184, 0.1); -fx-background-radius: 4px; " +
                                      "-fx-padding: 3px 6px; -fx-border-radius: 4px; -fx-border-color: rgba(148, 163, 184, 0.3);");
                    
                    // Add all components to details box
                    detailsBox.getChildren().addAll(seatsLabel, timeLabel, otpLabel, statusLabel);
                    
                    // Add passenger name and details to the main card
                    bookingCard.getChildren().addAll(passengerNameLabel, detailsBox);
                    
                    setGraphic(bookingCard);
                }
            }
        });
        
        new Thread(() -> {
            try {
                List<Ride> myRides = rideService.getMyRides();
                
                // Check if a specific ride was selected from MyRides view
                Long selectedRideId = SessionManager.getSelectedRideId();
                final Ride[] rideToLoad = new Ride[1];
                
                if (selectedRideId != null) {
                    // Find the selected ride in the list
                    rideToLoad[0] = myRides.stream()
                            .filter(r -> r.getId().equals(selectedRideId))
                            .findFirst().orElse(null);
                    // Clear after loading
                    SessionManager.clearSelectedRideId();
                }
                
                // If no specific ride selected, find first active ride
                if (rideToLoad[0] == null) {
                    rideToLoad[0] = myRides.stream()
                            .filter(r -> r.getStatus() == RideStatus.PUBLISHED || 
                                         r.getStatus() == RideStatus.BOOKED || 
                                         r.getStatus() == RideStatus.IN_PROGRESS)
                            .findFirst().orElse(null);
                }

                Platform.runLater(() -> {
                    if (rideToLoad[0] != null) {
                        currentRide = rideToLoad[0];
                        routeLabel.setText(rideToLoad[0].getSource() + " -> " + rideToLoad[0].getDestination());
                        timeLabel.setText("Departure: " + (rideToLoad[0].getDepartureTime() != null ? rideToLoad[0].getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A"));
                        
                        // Load bookings first to get accurate seat count
                        loadBookingsForRide(rideToLoad[0].getId());
                    } else {
                        actionStatusLabel.setText("No ride found. Create a ride from the dashboard.");
                        actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                        
                        // Hide all action buttons
                        startRideBox.setVisible(false);
                        completeButton.setVisible(false);
                        ongoingButton.setVisible(false);
                        completedButton.setVisible(false);
                        cancelledButton.setVisible(false);
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
    
    private void loadBookingsForRide(Long rideId) {
        new Thread(() -> {
            try {
                System.out.println("Loading bookings for ride ID: " + rideId);
                
                // Check if user is authenticated
                if (SessionManager.getToken() == null) {
                    Platform.runLater(() -> {
                        actionStatusLabel.setText("Please login to view bookings.");
                        actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                    });
                    return;
                }
                
                List<Booking> bookings = bookingService.getBookingsForRide(rideId);
                System.out.println("Found " + bookings.size() + " bookings");
                
                Platform.runLater(() -> {
                    if (bookings.isEmpty()) {
                        actionStatusLabel.setText("No bookings found for this ride.");
                        actionStatusLabel.setStyle("-fx-text-fill: #f59e0b;");
                    } else {
                        actionStatusLabel.setText("Loaded " + bookings.size() + " booking(s)");
                        actionStatusLabel.setStyle("-fx-text-fill: #10b981;");
                        
                        // Calculate actual booked seats from booking data
                        int totalBookedSeats = bookings.stream().mapToInt(Booking::getSeatsBooked).sum();
                        
                        // Update ride details with correct seat count
                        if (currentRide != null) {
                            int availableSeats = currentRide.getTotalSeats() - totalBookedSeats;
                            seatsLabel.setText("Seats: " + totalBookedSeats + " Booked | " + availableSeats + " Left | " + currentRide.getTotalSeats() + " Total");
                            fareLabel.setText("Fare per seat: ₹" + String.format("%.2f", currentRide.getFarePerSeat()));
                            statusLabel.setText("Status: " + currentRide.getStatus());
                            actionStatusLabel.setText("");
                            
                            // Show appropriate buttons based on ride status and bookings
                            System.out.println("Setting up buttons for ride status: " + currentRide.getStatus());
                            if (currentRide.getStatus() == RideStatus.BOOKED) {
                                startRideBox.setVisible(true);
                                startRideBox.setManaged(true);
                                completeButton.setVisible(false);
                                completeButton.setManaged(false);
                                ongoingButton.setVisible(true);
                                completedButton.setVisible(true);
                                cancelledButton.setVisible(true);
                                actionStatusLabel.setText("Ride is BOOKED. You can start it with OTP or change status.");
                            } else if (currentRide.getStatus() == RideStatus.IN_PROGRESS) {
                                startRideBox.setVisible(false);
                                startRideBox.setManaged(false);
                                completeButton.setVisible(true);
                                completeButton.setManaged(true);
                                ongoingButton.setVisible(false);
                                completedButton.setVisible(true);
                                cancelledButton.setVisible(true);
                                actionStatusLabel.setText("Ride is IN PROGRESS. Select an action below.");
                            } else if (currentRide.getStatus() == RideStatus.PUBLISHED && !bookings.isEmpty()) {
                                // Show status transition options for published rides with bookings
                                startRideBox.setVisible(false);
                                startRideBox.setManaged(false);
                                completeButton.setVisible(true);
                                completeButton.setManaged(true);
                                ongoingButton.setVisible(true);
                                completedButton.setVisible(true);
                                cancelledButton.setVisible(true);
                                actionStatusLabel.setText("Ride is PUBLISHED with bookings.");
                            } else if (currentRide.getStatus() == RideStatus.PUBLISHED) {
                                startRideBox.setVisible(false);
                                startRideBox.setManaged(false);
                                completeButton.setVisible(false);
                                completeButton.setManaged(false);
                                ongoingButton.setVisible(true);
                                completedButton.setVisible(true);
                                cancelledButton.setVisible(true);
                                actionStatusLabel.setText("Ride is PUBLISHED. Waiting for bookings.");
                            } else if (currentRide.getStatus() == RideStatus.COMPLETED) {
                                startRideBox.setVisible(false);
                                startRideBox.setManaged(false);
                                completeButton.setVisible(false);
                                completeButton.setManaged(false);
                                ongoingButton.setVisible(false);
                                completedButton.setVisible(false);
                                cancelledButton.setVisible(false);
                                actionStatusLabel.setText("This ride is COMPLETED. No further actions available.");
                                actionStatusLabel.setStyle("-fx-text-fill: #10b981;");
                            } else if (currentRide.getStatus() == RideStatus.CANCELLED) {
                                startRideBox.setVisible(false);
                                startRideBox.setManaged(false);
                                completeButton.setVisible(false);
                                completeButton.setManaged(false);
                                ongoingButton.setVisible(false);
                                completedButton.setVisible(false);
                                cancelledButton.setVisible(false);
                                actionStatusLabel.setText("This ride is CANCELLED. No further actions available.");
                                actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                            } else {
                                startRideBox.setVisible(false);
                                completeButton.setVisible(false);
                                ongoingButton.setVisible(false);
                                completedButton.setVisible(false);
                                cancelledButton.setVisible(false);
                            }
                        }
                    }
                    bookingsListView.getItems().setAll(bookings);
                });
            } catch (Exception e) {
                System.err.println("Error loading bookings: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                        actionStatusLabel.setText("Authentication failed. Please login again.");
                        actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                    } else {
                        actionStatusLabel.setText("Error loading bookings: " + e.getMessage());
                        actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                    }
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
        if (currentRide == null) return;
        
        // Show payment popup for completing ride
        showPaymentPopup();
    }
    
    private void showPaymentPopup() {
        PaymentPopupController paymentPopup = new PaymentPopupController();
        paymentPopup.showPaymentQR(currentRide);
    }

    @FXML
    private void handleCancelRide() {
        updateRideStatus(RideStatus.CANCELLED);
    }

    @FXML
    private void handleSetOngoing() {
        updateRideStatus(RideStatus.IN_PROGRESS);
    }

    @FXML
    private void handleSetCompleted() {
        updateRideStatus(RideStatus.COMPLETED);
    }

    @FXML
    private void handleSetCancelled() {
        updateRideStatus(RideStatus.CANCELLED);
    }

    @FXML
    private void handleViewPaymentStatus() {
        if (currentRide == null) return;
        
        showPaymentStatusForRide();
    }

    private void showPaymentStatusForRide() {
        Stage statusStage = new Stage();
        statusStage.initModality(Modality.APPLICATION_MODAL);
        statusStage.setTitle("Payment Status - Ride #" + currentRide.getId());
        
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #ffffff;");
        
        Label titleLabel = new Label("Payment Status for Ride #" + currentRide.getId());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label routeLabel = new Label(currentRide.getSource() + " -> " + currentRide.getDestination());
        routeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        
        Label loadingLabel = new Label("Loading payment data...");
        loadingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        
        // Payment details container
        VBox paymentDetailsBox = new VBox(10);
        paymentDetailsBox.setAlignment(Pos.CENTER);
        paymentDetailsBox.setStyle("-fx-background-color: #f9fafb; -fx-padding: 20px; -fx-background-radius: 8px;");
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        closeButton.setOnAction(e -> statusStage.close());
        
        layout.getChildren().addAll(titleLabel, routeLabel, loadingLabel, paymentDetailsBox, closeButton);
        
        Scene scene = new Scene(layout, 500, 600);
        statusStage.setScene(scene);
        statusStage.show();
        
        // Load payment data asynchronously
        new Thread(() -> {
            try {
                List<com.carpool.frontend.model.Payment> payments = new com.carpool.frontend.service.PaymentService().getPaymentsForRide(currentRide.getId());
                
                Platform.runLater(() -> {
                    paymentDetailsBox.getChildren().clear();
                    
                    if (payments.isEmpty()) {
                        Label noPaymentsLabel = new Label("No payments found for this ride");
                        noPaymentsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
                        paymentDetailsBox.getChildren().add(noPaymentsLabel);
                    } else {
                        Label paymentStatusTitle = new Label("Payment Details");
                        paymentStatusTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #374151;");
                        paymentDetailsBox.getChildren().add(paymentStatusTitle);
                        
                        double totalAmount = 0;
                        int completedPayments = 0;
                        
                        for (com.carpool.frontend.model.Payment payment : payments) {
                            VBox paymentCard = new VBox(8);
                            paymentCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 1px; " +
                                               "-fx-background-radius: 8px; -fx-padding: 15px; -fx-margin: 5px 0;");
                            
                            Label passengerLabel = new Label("Passenger: " + (payment.getPassengerName() != null ? payment.getPassengerName() : "Unknown"));
                            passengerLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1f2937;");
                            
                            Label amountLabel = new Label("Amount: ₹" + String.format("%.2f", payment.getAmount()));
                            amountLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
                            
                            Label statusLabel = new Label("Status: " + payment.getStatus());
                            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + 
                                (payment.getStatus().toString().equals("COMPLETED") ? "#10b981" : "#f59e0b") + "; -fx-font-weight: 500;");
                            
                            Label methodLabel = new Label("Method: " + (payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A"));
                            methodLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                            
                            paymentCard.getChildren().addAll(passengerLabel, amountLabel, statusLabel, methodLabel);
                            paymentDetailsBox.getChildren().add(paymentCard);
                            
                            totalAmount += payment.getAmount();
                            if (payment.getStatus().toString().equals("COMPLETED")) {
                                completedPayments++;
                            }
                        }
                        
                        // Summary
                        HBox summaryBox = new HBox(20);
                        summaryBox.setAlignment(Pos.CENTER);
                        summaryBox.setStyle("-fx-background-color: #f3f4f6; -fx-padding: 15px; -fx-background-radius: 8px; -fx-margin: 10px 0;");
                        
                        Label totalLabel = new Label("Total: ₹" + String.format("%.2f", totalAmount));
                        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
                        
                        Label completedLabel = new Label("Completed: " + completedPayments + "/" + payments.size());
                        completedLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10b981; -fx-font-weight: 600;");
                        
                        summaryBox.getChildren().addAll(totalLabel, completedLabel);
                        paymentDetailsBox.getChildren().add(summaryBox);
                    }
                    
                    layout.getChildren().remove(loadingLabel);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    paymentDetailsBox.getChildren().clear();
                    Label errorLabel = new Label("Error loading payment data: " + e.getMessage());
                    errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; -fx-font-style: italic;");
                    paymentDetailsBox.getChildren().add(errorLabel);
                    layout.getChildren().remove(loadingLabel);
                });
            }
        }).start();
    }

    private void updateRideStatus(RideStatus status) {
        if (currentRide == null) {
            System.err.println("Cannot update status: currentRide is null");
            return;
        }

        System.out.println("Updating ride " + currentRide.getId() + " to status: " + status);

        actionStatusLabel.setText("Updating status to " + status + "...");
        actionStatusLabel.setStyle("-fx-text-fill: #f59e0b;");

        // Disable all buttons during update
        setButtonsEnabled(false);

        new Thread(() -> {
            try {
                Ride updatedRide = rideService.updateRideStatus(currentRide.getId(), status);
                System.out.println("Status updated successfully: " + updatedRide.getStatus());

                Platform.runLater(() -> {
                    // Update the current ride with fresh data from backend
                    currentRide.setStatus(updatedRide.getStatus());
                    actionStatusLabel.setText("Status updated to " + status + " successfully!");
                    actionStatusLabel.setStyle("-fx-text-fill: #10b981;");
                    statusLabel.setText("Status: " + updatedRide.getStatus());

                    // Refresh button visibility based on new status
                    refreshButtonVisibility();

                    // Reload bookings to get updated booking statuses
                    loadBookingsForRide(currentRide.getId());
                });
            } catch (Exception e) {
                System.err.println("Error updating status: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    actionStatusLabel.setText("Error updating status: " + e.getMessage());
                    actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }
    
    private void setButtonsEnabled(boolean enabled) {
        ongoingButton.setDisable(!enabled);
        completedButton.setDisable(!enabled);
        cancelledButton.setDisable(!enabled);
        completeButton.setDisable(!enabled);
    }
    
    private void refreshButtonVisibility() {
        // Re-evaluate button visibility based on current status
        if (currentRide == null) return;

        RideStatus status = currentRide.getStatus();
        System.out.println("Refreshing button visibility for status: " + status);

        if (status == RideStatus.IN_PROGRESS) {
            startRideBox.setVisible(false);
            startRideBox.setManaged(false);
            ongoingButton.setVisible(false);
            ongoingButton.setManaged(false);
            completeButton.setVisible(true);
            completeButton.setManaged(true);
            completedButton.setVisible(true);
            completedButton.setManaged(true);
            cancelledButton.setVisible(true);
            cancelledButton.setManaged(true);
            actionStatusLabel.setText("Ride is now IN PROGRESS.");
        } else if (status == RideStatus.COMPLETED) {
            startRideBox.setVisible(false);
            startRideBox.setManaged(false);
            ongoingButton.setVisible(false);
            ongoingButton.setManaged(false);
            completedButton.setVisible(false);
            completedButton.setManaged(false);
            cancelledButton.setVisible(false);
            cancelledButton.setManaged(false);
            completeButton.setVisible(false);
            completeButton.setManaged(false);
            actionStatusLabel.setText("Ride is now COMPLETED.");
            actionStatusLabel.setStyle("-fx-text-fill: #10b981;");
        } else if (status == RideStatus.CANCELLED) {
            startRideBox.setVisible(false);
            startRideBox.setManaged(false);
            ongoingButton.setVisible(false);
            ongoingButton.setManaged(false);
            completedButton.setVisible(false);
            completedButton.setManaged(false);
            cancelledButton.setVisible(false);
            cancelledButton.setManaged(false);
            completeButton.setVisible(false);
            completeButton.setManaged(false);
            actionStatusLabel.setText("Ride is now CANCELLED.");
            actionStatusLabel.setStyle("-fx-text-fill: #ef4444;");
        } else if (status == RideStatus.PUBLISHED) {
            startRideBox.setVisible(false);
            startRideBox.setManaged(false);
            ongoingButton.setVisible(true);
            ongoingButton.setManaged(true);
            completedButton.setVisible(true);
            completedButton.setManaged(true);
            cancelledButton.setVisible(true);
            cancelledButton.setManaged(true);
            completeButton.setVisible(false);
            completeButton.setManaged(false);
        } else if (status == RideStatus.BOOKED) {
            startRideBox.setVisible(true);
            startRideBox.setManaged(true);
            ongoingButton.setVisible(true);
            ongoingButton.setManaged(true);
            completedButton.setVisible(true);
            completedButton.setManaged(true);
            cancelledButton.setVisible(true);
            cancelledButton.setManaged(true);
            completeButton.setVisible(false);
            completeButton.setManaged(false);
        }

        setButtonsEnabled(true);
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }
    
    @FXML
    private void refreshRide() {
        System.out.println("Refreshing ride data...");
        if (currentRide != null) {
            loadBookingsForRide(currentRide.getId());
        } else {
            // Reload from scratch
            initialize();
        }
    }
}
