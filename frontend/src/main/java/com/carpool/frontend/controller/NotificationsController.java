package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Notifications Controller
 * Handles user notifications and alerts
 * Follows Single Responsibility Principle
 */
public class NotificationsController {

    @FXML private ListView<String> notificationsListView;
    @FXML private Label unreadCountLabel;
    @FXML private Button markAllAsReadButton;
    @FXML private Button clearAllButton;

    private List<Notification> notifications = new ArrayList<>();
    private int unreadCount = 0;

    @FXML
    public void initialize() {
        loadNotifications();
        updateUnreadCount();
        setupListView();
    }
    
    private void loadNotifications() {
        // In a real application, this would fetch from the backend
        // For demo purposes, we'll use sample data
        notifications.add(new Notification("Your ride from Main Gate to Library has been confirmed!", 
                                          LocalDateTime.now().minusHours(2), false));
        notifications.add(new Notification("Driver John Doe is starting your ride in 15 minutes", 
                                          LocalDateTime.now().minusMinutes(30), false));
        notifications.add(new Notification("Payment of $25.00 processed successfully", 
                                          LocalDateTime.now().minusHours(1), true));
        notifications.add(new Notification("You received a 5-star rating from your last passenger", 
                                          LocalDateTime.now().minusDays(1), true));
        notifications.add(new Notification("New ride available matching your preferences", 
                                          LocalDateTime.now().minusDays(2), true));
        
        updateNotificationsList();
    }
    
    private void setupListView() {
        notificationsListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    markAsRead(newValue);
                }
            }
        );
    }
    
    private void updateNotificationsList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        
        for (Notification notification : notifications) {
            String prefix = notification.isRead() ? "[READ] " : "[UNREAD] ";
            String formattedNotification = prefix + notification.getMessage() + 
                                         " (" + notification.getTimestamp().format(formatter) + ")";
            items.add(formattedNotification);
        }
        
        notificationsListView.setItems(items);
    }
    
    private void updateUnreadCount() {
        unreadCount = (int) notifications.stream().filter(n -> !n.isRead()).count();
        unreadCountLabel.setText("Unread: " + unreadCount);
    }
    
    private void markAsRead(String notificationText) {
        // Extract the actual message without prefix and timestamp
        String message = notificationText.replaceFirst("^\\[(READ|UNREAD)\\] ", "")
                                       .replaceAll(" \\(.*\\)$", "");
        
        for (Notification notification : notifications) {
            if (notification.getMessage().equals(message)) {
                notification.setRead(true);
                updateNotificationsList();
                updateUnreadCount();
                break;
            }
        }
    }

    @FXML
    private void markAllAsRead() {
        notifications.forEach(n -> n.setRead(true));
        updateNotificationsList();
        updateUnreadCount();
    }
    
    @FXML
    private void clearAll() {
        notifications.clear();
        updateNotificationsList();
        updateUnreadCount();
    }
    
    @FXML
    private void refreshNotifications() {
        loadNotifications();
    }
    
    @FXML
    private void goBack() throws IOException {
        App.setRoot("dashboard");
    }
    
    /**
     * Simple data class for notifications
     */
    private static class Notification {
        private String message;
        private LocalDateTime timestamp;
        private boolean read;
        
        public Notification(String message, LocalDateTime timestamp, boolean read) {
            this.message = message;
            this.timestamp = timestamp;
            this.read = read;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }
}
