package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Preferences Controller
 * Handles user preferences and settings
 * Follows Single Responsibility Principle
 */
public class PreferencesController {

    @FXML private ComboBox<String> languageComboBox;
    @FXML private ComboBox<String> currencyComboBox;
    @FXML private CheckBox emailNotificationsCheckBox;
    @FXML private CheckBox smsNotificationsCheckBox;
    @FXML private CheckBox pushNotificationsCheckBox;
    @FXML private CheckBox locationSharingCheckBox;
    @FXML private CheckBox profileViewCheckBox;
    @FXML private ComboBox<String> seatTypeComboBox;
    @FXML private CheckBox musicCheckBox;
    @FXML private CheckBox smokingCheckBox;
    @FXML private CheckBox petsCheckBox;
    @FXML private CheckBox conversationCheckBox;
    @FXML private Slider maxFareSlider;
    @FXML private Label maxFareLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private CheckBox autoPayCheckBox;

    private Map<String, Object> currentPreferences = new HashMap<>();

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupSlider();
        loadCurrentPreferences();
    }
    
    private void setupComboBoxes() {
        languageComboBox.setItems(FXCollections.observableArrayList(
            "English", "Spanish", "French", "German", "Chinese", "Japanese"
        ));
        languageComboBox.setValue("English");
        
        currencyComboBox.setItems(FXCollections.observableArrayList(
            "USD", "EUR", "GBP", "JPY", "CNY", "INR"
        ));
        currencyComboBox.setValue("USD");
        
        seatTypeComboBox.setItems(FXCollections.observableArrayList(
            "Regular", "Premium", "Window", "Front Seat"
        ));
        seatTypeComboBox.setValue("Regular");
        
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
            "Credit Card", "Debit Card", "PayPal", "Digital Wallet", "Cash"
        ));
        paymentMethodComboBox.setValue("Credit Card");
    }
    
    private void setupSlider() {
        maxFareSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            maxFareLabel.setText(String.format("$%.2f", newValue.doubleValue()));
        });
    }
    
    private void loadCurrentPreferences() {
        // In a real application, this would fetch from the backend
        // For demo purposes, we'll use default values
        emailNotificationsCheckBox.setSelected(true);
        smsNotificationsCheckBox.setSelected(false);
        pushNotificationsCheckBox.setSelected(true);
        locationSharingCheckBox.setSelected(true);
        profileViewCheckBox.setSelected(true);
        musicCheckBox.setSelected(true);
        smokingCheckBox.setSelected(false);
        petsCheckBox.setSelected(false);
        conversationCheckBox.setSelected(true);
        maxFareSlider.setValue(50.0);
        autoPayCheckBox.setSelected(false);
    }

    @FXML
    private void savePreferences() {
        // Collect all preferences
        currentPreferences.put("language", languageComboBox.getValue());
        currentPreferences.put("currency", currencyComboBox.getValue());
        currentPreferences.put("notificationEmail", emailNotificationsCheckBox.isSelected());
        currentPreferences.put("notificationSms", smsNotificationsCheckBox.isSelected());
        currentPreferences.put("notificationPush", pushNotificationsCheckBox.isSelected());
        currentPreferences.put("allowLocationSharing", locationSharingCheckBox.isSelected());
        currentPreferences.put("allowProfileView", profileViewCheckBox.isSelected());
        currentPreferences.put("preferredSeatType", seatTypeComboBox.getValue());
        currentPreferences.put("allowMusic", musicCheckBox.isSelected());
        currentPreferences.put("allowSmoking", smokingCheckBox.isSelected());
        currentPreferences.put("allowPets", petsCheckBox.isSelected());
        currentPreferences.put("allowConversation", conversationCheckBox.isSelected());
        currentPreferences.put("maxFareRange", maxFareSlider.getValue());
        currentPreferences.put("preferredPaymentMethod", paymentMethodComboBox.getValue());
        currentPreferences.put("autoPay", autoPayCheckBox.isSelected());
        
        // In a real application, this would save to the backend
        System.out.println("Saving preferences: " + currentPreferences);
        
        // Show success message
        maxFareLabel.setText("Preferences Saved!");
        
        // Reset after 2 seconds
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(2000);
                maxFareLabel.setText(String.format("$%.2f", maxFareSlider.getValue()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    @FXML
    private void resetToDefaults() {
        // Reset all fields to default values
        languageComboBox.setValue("English");
        currencyComboBox.setValue("USD");
        emailNotificationsCheckBox.setSelected(true);
        smsNotificationsCheckBox.setSelected(false);
        pushNotificationsCheckBox.setSelected(true);
        locationSharingCheckBox.setSelected(true);
        profileViewCheckBox.setSelected(true);
        seatTypeComboBox.setValue("Regular");
        musicCheckBox.setSelected(true);
        smokingCheckBox.setSelected(false);
        petsCheckBox.setSelected(false);
        conversationCheckBox.setSelected(true);
        maxFareSlider.setValue(50.0);
        paymentMethodComboBox.setValue("Credit Card");
        autoPayCheckBox.setSelected(false);
        
        System.out.println("Preferences reset to defaults");
    }
    
    @FXML
    private void goBack() throws IOException {
        App.setRoot("dashboard");
    }
}
