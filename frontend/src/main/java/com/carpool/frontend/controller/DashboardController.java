package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.model.Booking;
import com.carpool.frontend.service.RideService;
import com.carpool.frontend.service.BookingService;
import com.carpool.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private VBox riderMenu;
    @FXML private VBox driverMenu;
    @FXML private Button driverActionButton;
    @FXML private VBox currentRideStatusBox;
    @FXML private Label currentRideStatusLabel;
    @FXML private Label currentRideDetailsLabel;
    @FXML private Button viewRideButton;
    @FXML private Button adminDashboardButton;

    private boolean hasActiveRide = false;
    private Booking currentBooking = null;
    private final RideService rideService = new RideService();
    private final BookingService bookingService = new BookingService();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            String role = SessionManager.getCurrentUser().getRole();
            String roleText = "PASSENGER".equalsIgnoreCase(role) ? "Passenger (can also drive)" : role;
            welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUser().getName() + "! (" + roleText + ")");

            // Show admin dashboard button for admin users
            if ("ADMIN".equalsIgnoreCase(role)) {
                adminDashboardButton.setVisible(true);
                adminDashboardButton.setManaged(true);
            } else {
                adminDashboardButton.setVisible(false);
                adminDashboardButton.setManaged(false);
            }

            if ("DRIVER".equalsIgnoreCase(role) || "PASSENGER".equalsIgnoreCase(role)) {
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

                // Start monitoring passenger ride status
                startPassengerRideStatusMonitoring();
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
    private void goToAdminDashboard() throws IOException {
        App.setRoot("admin_dashboard");
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
    private void goToViewMyRides() throws IOException {
        App.setRoot("my_rides");
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
    
    private void startPassengerRideStatusMonitoring() {
        // Initial check
        checkPassengerRideStatus();
        
        // Start periodic monitoring (every 10 seconds)
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000); // Check every 10 seconds
                    checkPassengerRideStatus();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error monitoring ride status: " + e.getMessage());
                }
            }
        }).start();
    }
    
    private void checkPassengerRideStatus() {
        new Thread(() -> {
            try {
                if (SessionManager.getToken() == null) {
                    return;
                }
                
                List<Booking> myBookings = bookingService.getMyBookings();
                Booking activeBooking = myBookings.stream()
                    .filter(b -> b.getStatus().toString().equals("CONFIRMED") ||
                                b.getStatus().toString().equals("PAID") ||
                                b.getStatus().toString().equals("IN_PROGRESS") ||
                                b.getStatus().toString().equals("COMPLETED"))
                    .findFirst().orElse(null);
                
                Platform.runLater(() -> {
                    if (activeBooking != null) {
                        updatePassengerRideStatus(activeBooking);
                    } else {
                        clearPassengerRideStatus();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error checking passenger ride status: " + e.getMessage());
            }
        }).start();
    }
    
    private void updatePassengerRideStatus(Booking booking) {
        currentBooking = booking;
        
        String statusText = "";
        String detailsText = "";
        String statusColor = "";
        String paymentInfo = "";
        
        // Build payment status info
        if (booking.getPaymentStatus() != null) {
            String paymentStatusColor = booking.getPaymentStatus().equals("COMPLETED") ? "#10b981" : 
                                       booking.getPaymentStatus().equals("FAILED") ? "#ef4444" : "#f59e0b";
            String paymentStatusText = booking.getPaymentStatus().equals("PROCESSING") ? "Waiting for confirmation" : 
                                      booking.getPaymentStatus();
            paymentInfo = String.format("\nPayment: %s (%s)", 
                paymentStatusText,
                booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "N/A");
            if (booking.getPaymentAmount() != null) {
                paymentInfo += String.format(" - $%.2f", booking.getPaymentAmount());
            }
        }
        
        switch (booking.getStatus().toString()) {
            case "CONFIRMED":
                statusText = "Ride Confirmed";
                detailsText = String.format("%s -> %s\nDeparture: %s\nSeats: %d\nFare: $%.2f%s", 
                    booking.getSource(), booking.getDestination(),
                    booking.getOtpGeneratedAt() != null ? 
                        booking.getOtpGeneratedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : "N/A",
                    booking.getSeatsBooked(), booking.getTotalFare(), paymentInfo);
                statusColor = "#10b981"; // Green
                break;
            case "PAID":
                statusText = "Payment Completed";
                detailsText = String.format("%s -> %s\nDeparture: %s\nSeats: %d\nFare: $%.2f%s\nWaiting for ride to start", 
                    booking.getSource(), booking.getDestination(),
                    booking.getOtpGeneratedAt() != null ? 
                        booking.getOtpGeneratedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : "N/A",
                    booking.getSeatsBooked(), booking.getTotalFare(), paymentInfo);
                statusColor = "#10b981"; // Green
                break;
            case "IN_PROGRESS":
                statusText = "Ride In Progress";
                detailsText = String.format("%s -> %s\nStarted at: %s\nSeats: %d\nFare: $%.2f%s", 
                    booking.getSource(), booking.getDestination(),
                    booking.getWaitingStartedAt() != null ? 
                        booking.getWaitingStartedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : "N/A",
                    booking.getSeatsBooked(), booking.getTotalFare(), paymentInfo);
                statusColor = "#f59e0b"; // Orange
                break;
            case "COMPLETED":
                statusText = "Ride Completed";
                detailsText = String.format("%s -> %s\nFare: $%.2f%s\nRide completed successfully", 
                    booking.getSource(), booking.getDestination(), booking.getTotalFare(), paymentInfo);
                statusColor = "#10b981"; // Green
                break;
            case "CANCELLED":
                statusText = "Ride Cancelled";
                detailsText = String.format("%s -> %s\nRide was cancelled", 
                    booking.getSource(), booking.getDestination());
                statusColor = "#ef4444"; // Red
                break;
            default:
                statusText = "No Active Ride";
                detailsText = "";
                statusColor = "#94a3b8"; // Gray
        }
        
        currentRideStatusLabel.setText(statusText);
        currentRideStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
        currentRideDetailsLabel.setText(detailsText);
        currentRideDetailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        
        // Show appropriate buttons based on status and payment status
        boolean needsPayment = booking.getPaymentStatus() == null || 
                            booking.getPaymentStatus().equals("FAILED");
        
        if (needsPayment) {
            viewRideButton.setVisible(true);
            viewRideButton.setText(booking.getPaymentStatus() != null && 
                                   booking.getPaymentStatus().equals("FAILED") ? "Retry Payment" : "Pay Now");
        } else if (booking.getPaymentStatus() != null && booking.getPaymentStatus().equals("PROCESSING")) {
            viewRideButton.setVisible(true);
            viewRideButton.setText("Payment Pending");
            viewRideButton.setDisable(true);
        } else if (booking.getStatus().toString().equals("PAID") || 
                   booking.getStatus().toString().equals("IN_PROGRESS")) {
            viewRideButton.setVisible(true);
            viewRideButton.setText("View Details");
            viewRideButton.setDisable(false);
        } else {
            viewRideButton.setVisible(false);
        }
    }
    
    private void clearPassengerRideStatus() {
        currentBooking = null;
        currentRideStatusLabel.setText("No Active Ride");
        currentRideStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        currentRideDetailsLabel.setText("");
        viewRideButton.setVisible(false);
    }
    
    @FXML
    private void viewCurrentRide() throws IOException {
        if (currentBooking != null) {
            boolean needsPayment = currentBooking.getPaymentStatus() == null || 
                                currentBooking.getPaymentStatus().equals("FAILED") ||
                                currentBooking.getPaymentStatus().equals("PROCESSING");
            
            if (needsPayment) {
                // Show payment popup for rides that need payment
                showPaymentPopup();
            } else {
                // For now, show search rides since ride_details doesn't exist
                App.setRoot("search_rides");
            }
        }
    }
    
    private void showPaymentPopup() {
        if (currentBooking == null) return;
        
        // Create payment popup for passenger
        PaymentPopupController paymentPopup = new PaymentPopupController();
        paymentPopup.showPassengerPaymentPopup(currentBooking);
    }
}
