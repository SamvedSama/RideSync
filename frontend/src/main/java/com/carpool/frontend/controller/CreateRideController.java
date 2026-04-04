package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.service.LocationService;
import com.carpool.frontend.service.RideService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateRideController {

    @FXML private TextField sourceField;
    @FXML private TextField destinationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField seatsField;
    @FXML private TextField priceField;
    @FXML private Label statusLabel;
    
    private final LocationService locationService = new LocationService();
    private final RideService rideService = new RideService();

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
        
        datePicker.setValue(java.time.LocalDate.now());
        timeField.setText(java.time.LocalTime.now().plusMinutes(30).format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @FXML
    private void handleCreateRide() {
        String source = sourceField.getText();
        String dest = destinationField.getText();
        
        if(source.isEmpty() || dest.isEmpty() || datePicker.getValue() == null || timeField.getText().isEmpty() || seatsField.getText().isEmpty() || priceField.getText().isEmpty()) {
            statusLabel.setText("All fields are required!");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }
        
        statusLabel.setText("Verifying route and creating ride...");
        statusLabel.setStyle("-fx-text-fill: #f59e0b;");

        new Thread(() -> {
            boolean srcValid = locationService.isValidLocation(source);
            boolean dstValid = locationService.isValidLocation(dest);
            
            if(!srcValid || !dstValid) {
                 Platform.runLater(() -> {
                     statusLabel.setText("Invalid Source or Destination. Cannot find on map.");
                     statusLabel.setStyle("-fx-text-fill: #ef4444;");
                 });
                 return;
            }

            try {
                Ride ride = new Ride();
                ride.setSource(source);
                ride.setDestination(dest);
                ride.setTotalSeats(Integer.parseInt(seatsField.getText()));
                ride.setFarePerSeat(Double.parseDouble(priceField.getText()));
                String dateTimeStr = datePicker.getValue().toString() + " " + timeField.getText();
                ride.setDepartureTime(LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                rideService.createRide(ride);

                Platform.runLater(() -> {
                    statusLabel.setText("Ride successfully created!");
                    statusLabel.setStyle("-fx-text-fill: #10b981;");
                    try {
                        Thread.sleep(1000);
                        goToDashboard();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                     statusLabel.setText("Failed: " + e.getMessage());
                     statusLabel.setStyle("-fx-text-fill: #ef4444;");
                });
            }
        }).start();
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }
}
