package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.util.SessionManager;
import com.carpool.frontend.model.Booking;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.service.HistoryService;
import com.carpool.frontend.service.PaymentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Ride History Controller
 * Handles user ride history and statistics
 * Follows Single Responsibility Principle
 */
public class RideHistoryController {

    @FXML private ListView<Booking> rideHistoryListView;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label totalRidesLabel;
    @FXML private Label completedRidesLabel;
    @FXML private Label cancelledRidesLabel;
    @FXML private Label totalSpentLabel;

    private List<Booking> bookingHistory = new ArrayList<>();
    private HistoryService historyService = new HistoryService();
    private PaymentService paymentService = new PaymentService();

    @FXML
    public void initialize() {
        setupFilterComboBox();
        setupRideHistoryListView();
        loadRideHistory();
        updateStatistics();
    }
    
    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Rides", "Completed", "Cancelled", "Last 30 Days", "Last 3 Months"
        ));
        filterComboBox.setValue("All Rides");
    }
    
    private void setupRideHistoryListView() {
        rideHistoryListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Booking booking, boolean empty) {
                super.updateItem(booking, empty);
                if (empty || booking == null) {
                    setGraphic(null);
                } else {
                    // Create enhanced booking history card
                    VBox historyCard = new VBox(10);
                    historyCard.setStyle("-fx-background-color: linear-gradient(to right, #1e293b, #0f172a); " +
                                       "-fx-background-radius: 12px; -fx-border-color: #334155; -fx-border-width: 1px; " +
                                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3); " +
                                       "-fx-padding: 15px; -fx-margin: 8px 0;");
                    
                    // Route info
                    Label routeLabel = new Label();
                    String route = booking.getSource() + " -> " + booking.getDestination();
                    routeLabel.setText(route);
                    routeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
                    
                    // Details container
                    VBox detailsBox = new VBox(6);
                    
                    // Status and date
                    Label statusLabel = new Label();
                    statusLabel.setText("Status: " + booking.getStatus());
                    statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
                    
                    // Date
                    Label dateLabel = new Label();
                    String date = booking.getOtpGeneratedAt() != null ? 
                        booking.getOtpGeneratedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) : "N/A";
                    dateLabel.setText("Date: " + date);
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
                    
                    // Fare and seats
                    Label fareLabel = new Label();
                    fareLabel.setText("Fare: $" + String.format("%.2f", booking.getTotalFare()) + " | Seats: " + booking.getSeatsBooked());
                    fareLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #10b981; " +
                                      "-fx-background-color: rgba(16, 185, 129, 0.15); -fx-background-radius: 6px; " +
                                      "-fx-padding: 4px 8px; -fx-border-radius: 6px; -fx-border-color: rgba(16, 185, 129, 0.4);");
                    
                    // Payment status
                    Label paymentLabel = new Label();
                    paymentLabel.setText("Payment: Processing...");
                    paymentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b; " +
                                        "-fx-background-color: rgba(245, 158, 11, 0.15); -fx-background-radius: 4px; " +
                                        "-fx-padding: 3px 6px; -fx-border-radius: 4px; -fx-border-color: rgba(245, 158, 11, 0.3);");
                    
                    detailsBox.getChildren().addAll(statusLabel, dateLabel, fareLabel, paymentLabel);
                    historyCard.getChildren().addAll(routeLabel, detailsBox);
                    
                    setGraphic(historyCard);
                }
            }
        });
    }
    
    private void loadRideHistory() {
        new Thread(() -> {
            try {
                if (SessionManager.getToken() == null) {
                    Platform.runLater(() -> {
                        totalRidesLabel.setText("Please login to view history");
                    });
                    return;
                }
                
                // Load booking history from backend
                bookingHistory = historyService.getPassengerRideHistory();
                
                Platform.runLater(() -> {
                    updateRideHistoryList();
                    updateStatistics();
                });
            } catch (Exception e) {
                System.err.println("Error loading ride history: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    totalRidesLabel.setText("Error loading history");
                });
            }
        }).start();
    }
    
    private void updateRideHistoryList() {
        ObservableList<Booking> items = FXCollections.observableArrayList(bookingHistory);
        rideHistoryListView.setItems(items);
    }
    
    private void updateStatistics() {
        int totalRides = bookingHistory.size();
        int completedRides = (int) bookingHistory.stream().filter(b -> b.getStatus().toString().equals("COMPLETED")).count();
        int cancelledRides = (int) bookingHistory.stream().filter(b -> b.getStatus().toString().equals("CANCELLED")).count();
        double totalSpent = bookingHistory.stream().mapToDouble(Booking::getTotalFare).sum();
        
        totalRidesLabel.setText("Total Rides: " + totalRides);
        completedRidesLabel.setText("Completed: " + completedRides);
        cancelledRidesLabel.setText("Cancelled: " + cancelledRides);
        totalSpentLabel.setText(String.format("Total Spent: $%.2f", totalSpent));
    }

    @FXML
    private void applyFilter() {
        String selectedFilter = filterComboBox.getValue();
        
        // In a real application, this would filter the data from the backend
        // For demo purposes, we'll just show a message
        System.out.println("Applying filter: " + selectedFilter);
        
        // You could implement actual filtering logic here
        updateRideHistoryList();
    }
    
    @FXML
    private void exportHistory() {
        // In a real application, this would generate and download a file
        System.out.println("Exporting ride history...");
        
        // Show confirmation
        totalRidesLabel.setText("History Exported Successfully!");
        
        // Reset after 3 seconds
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(3000);
                updateStatistics();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    @FXML
    private void refreshHistory() {
        loadRideHistory();
        updateStatistics();
    }
    
    @FXML
    private void goBack() throws IOException {
        App.setRoot("dashboard");
    }
    
    /**
     * Simple data class for ride history items
     */
    private static class RideHistoryItem {
        private String source;
        private String destination;
        private String status;
        private LocalDateTime date;
        private double fare;
        private double rating;
        
        public RideHistoryItem(String source, String destination, String status, 
                             LocalDateTime date, double fare, double rating) {
            this.source = source;
            this.destination = destination;
            this.status = status;
            this.date = date;
            this.fare = fare;
            this.rating = rating;
        }
        
        // Getters
        public String getSource() { return source; }
        public String getDestination() { return destination; }
        public String getStatus() { return status; }
        public LocalDateTime getDate() { return date; }
        public double getFare() { return fare; }
        public double getRating() { return rating; }
    }
}
