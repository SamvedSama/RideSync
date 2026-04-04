package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.service.RideService;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PaymentPopupController {

    private final RideService rideService = new RideService();

    public void showPaymentQR(Ride ride) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Ride Payment");

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #ffffff;");

        Label title = new Label("End Ride & Complete Payment");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label fareLabel = new Label("Amount due: ₹" + String.format("%.2f", ride.getFarePerSeat() * ride.getTotalSeats()));
        fareLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #10b981;");

        ImageView qrView = new ImageView();
        try {
            // Unauthed QR API generator
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=RidePayment_" + ride.getId();
            qrView.setImage(new Image(qrUrl, true));
        } catch (Exception e) {
            System.err.println("Failed to load QR code.");
        }

        Button confirmButton = new Button("Confirm Scanned / Complete");
        confirmButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        confirmButton.setOnAction(e -> {
            confirmButton.setDisable(true);
            completeRide(ride, popupStage);
        });

        layout.getChildren().addAll(title, fareLabel, qrView, confirmButton);

        Scene scene = new Scene(layout, 350, 450);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void completeRide(Ride ride, Stage popupStage) {
        new Thread(() -> {
            try {
                rideService.updateRideStatus(ride.getId(), RideStatus.COMPLETED);
                Platform.runLater(() -> {
                    popupStage.close();
                    try {
                        App.setRoot("dashboard");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Failed to complete ride: " + e.getMessage());
                });
            }
        }).start();
    }
}
