package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Emergency Contacts Controller
 * Handles emergency contact management and safety features
 * Follows Single Responsibility Principle
 */
public class EmergencyContactsController {

    @FXML private ListView<String> contactsListView;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> relationshipComboBox;
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button updateButton;
    @FXML private Button shareTripButton;
    @FXML private Button emergencyAlertButton;
    @FXML private Label statusLabel;
    @FXML private VBox contactFormVBox;

    private List<EmergencyContact> emergencyContacts = new ArrayList<>();
    private int selectedContactIndex = -1;

    @FXML
    public void initialize() {
        setupRelationshipComboBox();
        loadEmergencyContacts();
        setupListView();
    }
    
    private void setupRelationshipComboBox() {
        relationshipComboBox.setItems(FXCollections.observableArrayList(
            "Parent", "Spouse", "Sibling", "Friend", "Relative", "Colleague", "Other"
        ));
        relationshipComboBox.setValue("Friend");
    }
    
    private void loadEmergencyContacts() {
        // In a real application, this would fetch from the backend
        // For demo purposes, we'll use sample data
        emergencyContacts.add(new EmergencyContact("John Doe", "+1234567890", "Friend"));
        emergencyContacts.add(new EmergencyContact("Jane Smith", "+0987654321", "Sibling"));
        
        updateContactsList();
    }
    
    private void setupListView() {
        contactsListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedContactIndex = contactsListView.getSelectionModel().getSelectedIndex();
                    loadContactDetails(selectedContactIndex);
                }
            }
        );
    }
    
    private void updateContactsList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (EmergencyContact contact : emergencyContacts) {
            items.add(contact.getName() + " (" + contact.getRelationship() + ") - " + contact.getPhone());
        }
        contactsListView.setItems(items);
    }
    
    private void loadContactDetails(int index) {
        if (index >= 0 && index < emergencyContacts.size()) {
            EmergencyContact contact = emergencyContacts.get(index);
            nameField.setText(contact.getName());
            phoneField.setText(contact.getPhone());
            relationshipComboBox.setValue(contact.getRelationship());
            
            deleteButton.setDisable(false);
            updateButton.setDisable(false);
        }
    }
    
    private void clearForm() {
        nameField.clear();
        phoneField.clear();
        relationshipComboBox.setValue("Friend");
        selectedContactIndex = -1;
        deleteButton.setDisable(true);
        updateButton.setDisable(true);
    }
    
    private boolean validateForm() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String relationship = relationshipComboBox.getValue();
        
        if (name.isEmpty()) {
            showAlert("Error", "Please enter a name for the emergency contact.");
            return false;
        }
        
        if (phone.isEmpty()) {
            showAlert("Error", "Please enter a phone number.");
            return false;
        }
        
        if (!phone.matches("\\+?[0-9]{10,15}")) {
            showAlert("Error", "Please enter a valid phone number (10-15 digits).");
            return false;
        }
        
        if (relationship == null) {
            showAlert("Error", "Please select a relationship.");
            return false;
        }
        
        return true;
    }

    @FXML
    private void addContact() {
        if (!validateForm()) {
            return;
        }
        
        EmergencyContact newContact = new EmergencyContact(
            nameField.getText().trim(),
            phoneField.getText().trim(),
            relationshipComboBox.getValue()
        );
        
        emergencyContacts.add(newContact);
        updateContactsList();
        clearForm();
        
        statusLabel.setText("Emergency contact added successfully!");
        
        // In a real application, this would save to the backend
        System.out.println("Emergency contact saved: " + newContact);
    }
    
    @FXML
    private void updateContact() {
        if (selectedContactIndex < 0 || selectedContactIndex >= emergencyContacts.size()) {
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        EmergencyContact updatedContact = emergencyContacts.get(selectedContactIndex);
        updatedContact.setName(nameField.getText().trim());
        updatedContact.setPhone(phoneField.getText().trim());
        updatedContact.setRelationship(relationshipComboBox.getValue());
        
        updateContactsList();
        clearForm();
        
        statusLabel.setText("Emergency contact updated successfully!");
        
        // In a real application, this would update in the backend
        System.out.println("Emergency contact updated: " + updatedContact);
    }
    
    @FXML
    private void deleteContact() {
        if (selectedContactIndex < 0 || selectedContactIndex >= emergencyContacts.size()) {
            return;
        }
        
        EmergencyContact contactToDelete = emergencyContacts.get(selectedContactIndex);
        
        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Emergency Contact");
        confirmDialog.setContentText("Are you sure you want to delete " + contactToDelete.getName() + " from your emergency contacts?");
        
        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            emergencyContacts.remove(selectedContactIndex);
            updateContactsList();
            clearForm();
            
            statusLabel.setText("Emergency contact deleted successfully!");
            
            // In a real application, this would delete from the backend
            System.out.println("Emergency contact deleted: " + contactToDelete);
        }
    }
    
    @FXML
    private void shareTripDetails() {
        if (emergencyContacts.isEmpty()) {
            showAlert("No Contacts", "Please add emergency contacts first.");
            return;
        }
        
        // In a real application, this would share current trip details
        statusLabel.setText("Trip details shared with all emergency contacts!");
        
        // Simulate sharing trip details
        for (EmergencyContact contact : emergencyContacts) {
            System.out.println("Trip details shared with: " + contact.getName() + " (" + contact.getPhone() + ")");
        }
    }
    
    @FXML
    private void triggerEmergencyAlert() {
        if (emergencyContacts.isEmpty()) {
            showAlert("No Contacts", "Please add emergency contacts first.");
            return;
        }
        
        Alert emergencyDialog = new Alert(AlertType.WARNING);
        emergencyDialog.setTitle("Emergency Alert");
        emergencyDialog.setHeaderText("Trigger Emergency Alert");
        emergencyDialog.setContentText("This will immediately alert all your emergency contacts. Are you sure?");
        
        if (emergencyDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Trigger emergency alert
            statusLabel.setText("EMERGENCY ALERT SENT to all contacts!");
            
            // Simulate emergency alert
            for (EmergencyContact contact : emergencyContacts) {
                System.out.println("EMERGENCY ALERT sent to: " + contact.getName() + " (" + contact.getPhone() + ")");
            }
            
            // In a real application, this would:
            // 1. Send SMS/call to all emergency contacts
            // 2. Notify emergency services if needed
            // 3. Share current location
            // 4. Log the emergency alert
        }
    }
    
    @FXML
    private void testNotification() {
        if (emergencyContacts.isEmpty()) {
            showAlert("No Contacts", "Please add emergency contacts first.");
            return;
        }
        
        // Send test notification to first contact
        EmergencyContact testContact = emergencyContacts.get(0);
        statusLabel.setText("Test notification sent to " + testContact.getName());
        
        System.out.println("Test notification sent to: " + testContact.getName() + " (" + testContact.getPhone() + ")");
    }
    
    @FXML
    private void goBack() throws IOException {
        App.setRoot("dashboard");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Simple data class for emergency contacts
     */
    private static class EmergencyContact {
        private String name;
        private String phone;
        private String relationship;
        
        public EmergencyContact(String name, String phone, String relationship) {
            this.name = name;
            this.phone = phone;
            this.relationship = relationship;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        
        @Override
        public String toString() {
            return name + " (" + relationship + ") - " + phone;
        }
    }
}
