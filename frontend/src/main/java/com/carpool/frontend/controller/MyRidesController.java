package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.service.RideService;
import com.carpool.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * My Rides Controller - For drivers to view and manage all their created rides
 */
public class MyRidesController {

    @FXML private ListView<Ride> ridesListView;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label totalRidesLabel;
    @FXML private Label activeRidesLabel;
    @FXML private Label completedRidesLabel;
    @FXML private Label cancelledRidesLabel;
    @FXML private Label statusLabel;
    @FXML private Button manageButton;

    private List<Ride> myRides;
    private Ride selectedRide;
    private RideService rideService = new RideService();

    @FXML
    public void initialize() {
        setupFilterComboBox();
        setupRidesListView();
        loadMyRides();
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Rides", "Active", "Published", "Booked", "In Progress", "Completed", "Cancelled"
        ));
        filterComboBox.setValue("All Rides");
        filterComboBox.setOnAction(e -> applyFilter());
    }

    private void setupRidesListView() {
        ridesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Ride ride, boolean empty) {
                super.updateItem(ride, empty);
                if (empty || ride == null) {
                    setGraphic(null);
                } else {
                    VBox rideCard = new VBox(8);
                    
                    // Color based on status
                    String statusColor = getStatusColor(ride.getStatus());
                    String bgColor = getStatusBgColor(ride.getStatus());
                    
                    rideCard.setStyle("-fx-background-color: " + bgColor + "; " +
                                     "-fx-background-radius: 12px; -fx-border-color: " + statusColor + "; -fx-border-width: 2px; " +
                                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3); " +
                                     "-fx-padding: 15px; -fx-margin: 8px 0;");
                    
                    // Route info
                    Label routeLabel = new Label(ride.getSource() + " -> " + ride.getDestination());
                    routeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
                    
                    // Status badge
                    Label statusBadge = new Label(ride.getStatus().toString());
                    statusBadge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; " +
                                        "-fx-background-color: " + statusColor + "; -fx-background-radius: 6px; " +
                                        "-fx-padding: 4px 12px;");
                    
                    // Details
                    HBox detailsBox = new HBox(20);
                    
                    String departureTime = ride.getDepartureTime() != null ? 
                        ride.getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A";
                    
                    Label timeLabel = new Label("Departure: " + departureTime);
                    timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
                    
                    Label seatsLabel = new Label("Seats: " + ride.getTotalSeats());
                    seatsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
                    
                    Label fareLabel = new Label("Fare: ₹" + String.format("%.2f", ride.getFarePerSeat()));
                    fareLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-font-weight: 600;");
                    
                    detailsBox.getChildren().addAll(timeLabel, seatsLabel, fareLabel);
                    
                    // Click to select - use the ListView's selection model directly
                    rideCard.setOnMouseClicked(e -> {
                        // Get the index of this item and select it in the ListView
                        getListView().getSelectionModel().select(ride);
                        getListView().requestFocus();
                        
                        if (e.getClickCount() == 2) {
                            // Double click to manage
                            manageRide(ride);
                        }
                    });
                    
                    rideCard.getChildren().addAll(routeLabel, statusBadge, detailsBox);
                    setGraphic(rideCard);
                }
            }
        });
        
        // Single selection listener
        ridesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRide = newVal;
            manageButton.setDisable(newVal == null);
            if (newVal != null) {
                statusLabel.setText("Selected: " + newVal.getSource() + " -> " + newVal.getDestination());
            }
        });
    }
    
    private String getStatusColor(RideStatus status) {
        switch (status) {
            case PUBLISHED: return "#3b82f6"; // Blue
            case BOOKED: return "#f59e0b";    // Orange
            case IN_PROGRESS: return "#8b5cf6"; // Purple
            case COMPLETED: return "#10b981";  // Green
            case CANCELLED: return "#ef4444";  // Red
            default: return "#6b7280";         // Gray
        }
    }
    
    private String getStatusBgColor(RideStatus status) {
        switch (status) {
            case PUBLISHED: return "#1e3a5f";
            case BOOKED: return "#451a03";
            case IN_PROGRESS: return "#3b0764";
            case COMPLETED: return "#064e3b";
            case CANCELLED: return "#450a0a";
            default: return "#1e293b";
        }
    }

    private void loadMyRides() {
        statusLabel.setText("Loading your rides...");
        statusLabel.setStyle("-fx-text-fill: #f59e0b;");
        
        new Thread(() -> {
            try {
                if (SessionManager.getToken() == null) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Please login to view your rides");
                        statusLabel.setStyle("-fx-text-fill: #ef4444;");
                    });
                    return;
                }
                
                myRides = rideService.getMyRides();
                
                Platform.runLater(() -> {
                    updateRidesList();
                    updateStatistics();
                    statusLabel.setText("Loaded " + myRides.size() + " ride(s)");
                    statusLabel.setStyle("-fx-text-fill: #10b981;");
                });
            } catch (Exception e) {
                System.err.println("Error loading rides: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #ef4444;");
                });
            }
        }).start();
    }
    
    private void updateRidesList() {
        ObservableList<Ride> items = FXCollections.observableArrayList(myRides);
        ridesListView.setItems(items);
    }
    
    private void updateStatistics() {
        int total = myRides.size();
        int active = (int) myRides.stream().filter(r -> 
            r.getStatus() == RideStatus.PUBLISHED || 
            r.getStatus() == RideStatus.BOOKED || 
            r.getStatus() == RideStatus.IN_PROGRESS).count();
        int completed = (int) myRides.stream().filter(r -> r.getStatus() == RideStatus.COMPLETED).count();
        int cancelled = (int) myRides.stream().filter(r -> r.getStatus() == RideStatus.CANCELLED).count();
        
        totalRidesLabel.setText("Total: " + total);
        activeRidesLabel.setText("Active: " + active);
        completedRidesLabel.setText("Completed: " + completed);
        cancelledRidesLabel.setText("Cancelled: " + cancelled);
    }
    
    private void selectRide(Ride ride) {
        System.out.println("Selected ride: " + ride.getId() + " - " + ride.getSource() + " -> " + ride.getDestination());
        ridesListView.getSelectionModel().select(ride);
        selectedRide = ride;
        manageButton.setDisable(false);
        manageButton.setStyle("-fx-opacity: 1;");
    }
    
    @FXML
    private void applyFilter() {
        String filter = filterComboBox.getValue();
        if (filter == null || myRides == null) return;
        
        List<Ride> filtered;
        switch (filter) {
            case "Active":
                filtered = myRides.stream()
                    .filter(r -> r.getStatus() == RideStatus.PUBLISHED || 
                                r.getStatus() == RideStatus.BOOKED || 
                                r.getStatus() == RideStatus.IN_PROGRESS)
                    .toList();
                break;
            case "Published":
                filtered = myRides.stream().filter(r -> r.getStatus() == RideStatus.PUBLISHED).toList();
                break;
            case "Booked":
                filtered = myRides.stream().filter(r -> r.getStatus() == RideStatus.BOOKED).toList();
                break;
            case "In Progress":
                filtered = myRides.stream().filter(r -> r.getStatus() == RideStatus.IN_PROGRESS).toList();
                break;
            case "Completed":
                filtered = myRides.stream().filter(r -> r.getStatus() == RideStatus.COMPLETED).toList();
                break;
            case "Cancelled":
                filtered = myRides.stream().filter(r -> r.getStatus() == RideStatus.CANCELLED).toList();
                break;
            default:
                filtered = myRides;
        }
        
        ObservableList<Ride> items = FXCollections.observableArrayList(filtered);
        ridesListView.setItems(items);
        statusLabel.setText("Showing " + filtered.size() + " ride(s) - Filter: " + filter);
    }

    @FXML
    private void handleManageRide() {
        System.out.println("handleManageRide called, selectedRide = " + selectedRide);
        if (selectedRide != null) {
            manageRide(selectedRide);
        } else {
            statusLabel.setText("Please select a ride first");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }
    
    private void manageRide(Ride ride) {
        System.out.println("Managing ride: " + ride.getId() + " - " + ride.getSource() + " -> " + ride.getDestination());
        // Store selected ride in session for manage_ride controller to use
        SessionManager.setSelectedRideId(ride.getId());
        try {
            App.setRoot("manage_ride");
        } catch (IOException e) {
            System.err.println("Error opening manage_ride: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error opening ride management: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void refreshRides() {
        loadMyRides();
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("dashboard");
    }
}
