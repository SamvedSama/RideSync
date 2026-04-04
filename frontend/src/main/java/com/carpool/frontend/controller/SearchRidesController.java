package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.service.AuthService;
import com.carpool.frontend.service.LocationService;
import com.carpool.frontend.service.RideService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.controlsfx.control.textfield.TextFields;
import com.carpool.frontend.model.Booking;
import com.carpool.frontend.service.BookingService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SearchRidesController {

    @FXML private Label statusLabel;
    @FXML private TextField sourceField;
    @FXML private TextField destinationField;
    @FXML private ListView<Ride> ridesListView;
    @FXML private Button trackerButton;
    @FXML private HBox bookingBox;
    @FXML private Spinner<Integer> seatsSpinner;
    
    private final AuthService authService = new AuthService();
    private final LocationService locationService = new LocationService();
    private final RideService rideService = new RideService();
    private final BookingService bookingService = new BookingService();

    @FXML
    public void initialize() {
        TextFields.bindAutoCompletion(sourceField, request -> {
            if (request.getUserText().length() >= 3) {
                return locationService.fetchSuggestions(request.getUserText());
            }
            return java.util.Collections.emptyList();
        });

        TextFields.bindAutoCompletion(destinationField, request -> {
            if (request.getUserText().length() >= 3) {
                return locationService.fetchSuggestions(request.getUserText());
            }
            return java.util.Collections.emptyList();
        });

        ridesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Ride ride, boolean empty) {
                super.updateItem(ride, empty);
                if (empty || ride == null) {
                    setText(null);
                } else {
                    String time = ride.getDepartureTime() != null ? ride.getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A";
                    String driverName = ride.getDriver() != null ? ride.getDriver().getName() : "Unknown";
                    setText(String.format("%s -> %s | Departure: %s | Seats: %d | Fare: ₹%.2f | Driver: %s",
                            ride.getSource(), ride.getDestination(), time, ride.getAvailableSeats(), ride.getFarePerSeat(), driverName));
                }
            }
        });

        ridesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.getAvailableSeats() > 0) {
                    bookingBox.setVisible(true);
                    seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, newVal.getAvailableSeats(), 1));
                } else {
                    bookingBox.setVisible(false);
                }
                trackerButton.setVisible(false);
            } else {
                bookingBox.setVisible(false);
            }
        });
        
        // Initial search to load all
        performSearch(true);
    }

    @FXML
    private void handleSearch(javafx.event.ActionEvent event) {
        performSearch(false);
    }
    
    private void performSearch(boolean onLoad) {
        String src = sourceField.getText();
        String dst = destinationField.getText();
        
        if(!onLoad && (src.isEmpty() || dst.isEmpty())) {
            statusLabel.setText("Please enter source and destination.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        statusLabel.setText("Searching rides...");
        statusLabel.setStyle("-fx-text-fill: #f59e0b;");

        new Thread(() -> {
            try {
                if (!onLoad) {
                    boolean sv = locationService.isValidLocation(src);
                    boolean dv = locationService.isValidLocation(dst);
                    if (!sv || !dv) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Invalid Search: Target locations not found in India.");
                            statusLabel.setStyle("-fx-text-fill: #ef4444;");
                            ridesListView.setItems(FXCollections.observableArrayList());
                        });
                        return;
                    }
                }

                List<Ride> rides = rideService.searchRides(onLoad ? null : src, onLoad ? null : dst);
                
                Platform.runLater(() -> {
                    ridesListView.setItems(FXCollections.observableArrayList(rides));
                    statusLabel.setText("Found " + rides.size() + " rides.");
                    statusLabel.setStyle("-fx-text-fill: #10b981;");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error loading rides: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #ef4444;");
                    e.printStackTrace();
                });
            }
        }).start();
    }

    @FXML
    private void handleBookRide() {
        Ride selectedRide = ridesListView.getSelectionModel().getSelectedItem();
        if (selectedRide == null) return;
        
        statusLabel.setText("Booking...");
        statusLabel.setStyle("-fx-text-fill: #3b82f6;");
        bookingBox.setDisable(true);
        
        int seatsToBook = seatsSpinner.getValue();
        
        new Thread(() -> {
            try {
                Booking booking = bookingService.bookRide(selectedRide.getId(), seatsToBook);
                Platform.runLater(() -> {
                    bookingBox.setDisable(false);
                    bookingBox.setVisible(false);
                    trackerButton.setVisible(true);
                    statusLabel.setText("Success! Your Start OTP is: " + booking.getStartOtp());
                    statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 16px;");
                    performSearch(true); // refresh list
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    bookingBox.setDisable(false);
                    statusLabel.setText(e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #ef4444;");
                });
            }
        }).start();
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML
    private void goToMap() throws IOException {
        App.setRoot("map_view");
    }
}
