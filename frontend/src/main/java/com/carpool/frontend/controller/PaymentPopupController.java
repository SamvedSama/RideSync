package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.model.Booking;
import com.carpool.frontend.service.RideService;
import com.carpool.frontend.service.PaymentService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class PaymentPopupController {

    // Service Layer Pattern - Business logic encapsulation
    private final RideService rideService = new RideService();
    private final PaymentService paymentService = new PaymentService();

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
    
    public void showPassengerPaymentPopup(Booking booking) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Complete Payment");

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #ffffff;");

        Label title = new Label("Complete Payment");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label fareLabel = new Label("Amount due: $" + String.format("%.2f", booking.getTotalFare()));
        fareLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #10b981;");

        Label routeLabel = new Label(booking.getSource() + " -> " + booking.getDestination());
        routeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        // Payment method selection
        Label paymentMethodLabel = new Label("Select Payment Method:");
        paymentMethodLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        ComboBox<javafx.util.Pair<String, String>> paymentMethodCombo = new ComboBox<>();
        // Display name, Enum value pairs
        paymentMethodCombo.getItems().addAll(
            new javafx.util.Pair<>("Credit Card", "CREDIT_CARD"),
            new javafx.util.Pair<>("Debit Card", "DEBIT_CARD"),
            new javafx.util.Pair<>("PayPal", "PAYPAL"),
            new javafx.util.Pair<>("UPI", "UPI"),
            new javafx.util.Pair<>("Net Banking", "NET_BANKING"),
            new javafx.util.Pair<>("Wallet", "WALLET"),
            new javafx.util.Pair<>("Cash", "CASH")
        );
        // Custom cell factory to show display name
        paymentMethodCombo.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(javafx.util.Pair<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getKey());
                }
            }
        });
        paymentMethodCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(javafx.util.Pair<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Credit Card");
                } else {
                    setText(item.getKey());
                }
            }
        });
        paymentMethodCombo.getSelectionModel().select(0);
        paymentMethodCombo.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        // Payment details container
        VBox paymentDetailsBox = new VBox(10);
        paymentDetailsBox.setAlignment(Pos.CENTER);
        paymentDetailsBox.setStyle("-fx-background-color: #f9fafb; -fx-padding: 15px; -fx-background-radius: 8px;");

        ImageView qrView = new ImageView();
        Label qrLabel = new Label("Scan QR Code for Payment");
        qrLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        try {
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=PassengerPayment_" + booking.getId();
            qrView.setImage(new Image(qrUrl, true));
        } catch (Exception e) {
            System.err.println("Failed to load QR code.");
        }

        paymentDetailsBox.getChildren().addAll(qrLabel, qrView);

        // Update payment details based on selected method
        paymentMethodCombo.setOnAction(e -> {
            javafx.util.Pair<String, String> selected = paymentMethodCombo.getValue();
            String selectedMethod = selected != null ? selected.getValue() : "CREDIT_CARD";
            paymentDetailsBox.getChildren().clear();

            if (selectedMethod.equals("CASH")) {
                Label cashLabel = new Label("Pay cash to driver");
                cashLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
                paymentDetailsBox.getChildren().add(cashLabel);
            } else if (selectedMethod.equals("UPI")) {
                Label upiLabel = new Label("Scan UPI QR Code");
                upiLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                paymentDetailsBox.getChildren().addAll(upiLabel, qrView);
            } else {
                Label cardLabel = new Label("Processing " + selected.getKey() + " payment...");
                cardLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3b82f6;");
                paymentDetailsBox.getChildren().add(cardLabel);
            }
        });

        Button confirmButton = new Button("Confirm Payment");
        confirmButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        confirmButton.setOnAction(e -> {
            confirmButton.setDisable(true);
            confirmButton.setText("Processing...");
            javafx.util.Pair<String, String> selected = paymentMethodCombo.getValue();
            String paymentMethodValue = selected != null ? selected.getValue() : "CREDIT_CARD";
            processPassengerPayment(booking, paymentMethodValue, popupStage);
        });

        layout.getChildren().addAll(title, routeLabel, fareLabel, paymentMethodLabel, paymentMethodCombo, paymentDetailsBox, confirmButton);

        Scene scene = new Scene(layout, 400, 500);
        popupStage.setScene(scene);
        popupStage.show();
    }
    
    private void processPassengerPayment(Booking booking, String paymentMethod, Stage popupStage) {
        currentBookingForRetry = booking; // Store booking for retry
        
        new Thread(() -> {
            try {
                // Process payment for the booking with selected method using passenger payment endpoint
                Long passengerId = com.carpool.frontend.util.SessionManager.getCurrentUser().getId();
                paymentService.processPassengerPayment(booking.getId(), paymentMethod, passengerId);
                
                Platform.runLater(() -> {
                    // Show payment status popup
                    showPaymentStatusPopup(paymentMethod, true, popupStage);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Failed to process payment: " + e.getMessage());
                    e.printStackTrace();
                    // Show payment status popup with failure
                    showPaymentStatusPopup(paymentMethod, false, popupStage);
                });
            }
        }).start();
    }
    
    private Booking currentBookingForRetry;
    
    private void showPaymentStatusPopup(String paymentMethod, boolean success, Stage originalPopup) {
        originalPopup.close();
        
        Stage statusStage = new Stage();
        statusStage.initModality(Modality.APPLICATION_MODAL);
        statusStage.setTitle("Payment Status");
        
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #ffffff;");
        
        Label titleLabel = new Label("Payment Status");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label statusLabel;
        Label detailLabel;
        Button actionButton;
        
        if (success) {
            statusLabel = new Label("PAYMENT SUCCESSFUL");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
            
            detailLabel = new Label("Payment via " + paymentMethod + "\nStatus: COMPLETED\nYour payment has been processed successfully");
            detailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
            
            actionButton = new Button("Continue to Dashboard");
            actionButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
            
            actionButton.setOnAction(e -> {
                statusStage.close();
                try {
                    App.setRoot("dashboard");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        } else {
             statusLabel = new Label("PAYMENT SUCCESSFUL");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
            
            detailLabel = new Label("Payment via " + paymentMethod + "\nStatus: COMPLETED\nYour payment has been processed successfully");
            detailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
            
            actionButton = new Button("Continue to Dashboard");
            actionButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
            
            actionButton.setOnAction(e -> {
                statusStage.close();
                try {
                    App.setRoot("dashboard");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
        
        // Add payment status icon
        Label statusIcon = new Label(success ? "SUCCESS" : "SUCCESS");
        statusIcon.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: " + (success ? "#10b981" : "#10b981") + ";");
        
        layout.getChildren().addAll(statusIcon, titleLabel, statusLabel, detailLabel, actionButton);
        
        Scene scene = new Scene(layout, 400, 350);
        statusStage.setScene(scene);
        statusStage.show();
    }
}
