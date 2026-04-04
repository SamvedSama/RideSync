package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Booking;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.service.BookingService;
import com.carpool.frontend.service.LocationService;
import com.carpool.frontend.service.RideService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MapController {
    
    @FXML private WebView webView;
    @FXML private Label statusLabel;
    @FXML private Button paymentButton;
    
    private WebEngine engine;
    private final RideService rideService = new RideService();
    private final BookingService bookingService = new BookingService();
    private final LocationService locationService = new LocationService();
    private Ride activeRide;
    private boolean isDriver = false;

    @FXML
    public void initialize() {
        engine = webView.getEngine();
        URL url = getClass().getResource("/html/map.html");
        engine.load(url.toExternalForm());
        
        new Thread(this::fetchActiveRideData).start();
    }

    private void fetchActiveRideData() {
        try {
            List<Ride> myRides = rideService.getMyRides();
            activeRide = myRides.stream().filter(r -> r.getStatus() == RideStatus.IN_PROGRESS).findFirst().orElse(null);
            
            if (activeRide != null) {
                isDriver = true;
            } else {
                List<Booking> myBookings = bookingService.getMyBookings();
                Booking booking = myBookings.stream().filter(b -> b.getRideStatus() == RideStatus.IN_PROGRESS).findFirst().orElse(null);
                if (booking != null) {
                    activeRide = rideService.getRide(booking.getRideId());
                }
            }
            
            if (activeRide != null) {
                Platform.runLater(() -> {
                    if (isDriver) paymentButton.setVisible(true);
                    setupMapInteractions();
                });
            } else {
                Platform.runLater(() -> statusLabel.setText("No active ride found."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Failed to sync Ride Data."));
        }
    }

    private void setupMapInteractions() {
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                statusLabel.setText("Map Loaded. Locating...");
                startTrackingLoop();
            }
        });
    }

    private void startTrackingLoop() {
        new Thread(() -> {
            double[] startCoords = locationService.geocode(activeRide.getSource());
            double[] endCoords = locationService.geocode(activeRide.getDestination());
            
            Platform.runLater(() -> {
                Timeline timeline = new Timeline();
                double totalSteps = 60.0;
                double dLat = (endCoords[0] - startCoords[0]) / totalSteps;
                double dLng = (endCoords[1] - startCoords[1]) / totalSteps;
                
                final int[] currentStep = {0};
                
                KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
                    if (currentStep[0] <= totalSteps) {
                        double currentLat = startCoords[0] + (dLat * currentStep[0]);
                        double currentLng = startCoords[1] + (dLng * currentStep[0]);
                        engine.executeScript("updateDriverLocation(" + currentLat + ", " + currentLng + ")");
                        statusLabel.setText("Live Locating: Step " + currentStep[0]);
                        currentStep[0]++;
                    }
                });
                
                timeline.getKeyFrames().add(keyFrame);
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
            });
        }).start();
    }

    @FXML
    private void handlePayment() {
        try {
            PaymentPopupController popup = new PaymentPopupController();
            popup.showPaymentQR(activeRide);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }
}
