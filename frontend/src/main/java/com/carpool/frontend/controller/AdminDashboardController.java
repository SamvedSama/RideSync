package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.model.User;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.AdminAuditLog;
import com.carpool.frontend.service.AdminService;
import com.carpool.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

/**
 * AdminDashboardController - Handles admin operations UI
 *
 * DESIGN PATTERNS USED:
 * 1. MVC Pattern - Controller in Model-View-Controller architecture
 *    - Separates UI logic from business logic and data models
 *
 * 2. Service Layer Pattern - Uses AdminService
 *    - Encapsulates admin business logic
 *    - Controller delegates to service for API calls
 *
 * 3. Singleton Pattern - SessionManager (indirectly used)
 *    - Provides single source of truth for authentication state
 *
 * DESIGN PRINCIPLES:
 * 1. Single Responsibility Principle (SRP)
 *    - Each method handles one specific admin operation
 *
 * 2. Dependency Inversion Principle (DIP)
 *    - Depends on AdminService abstraction
 */
public class AdminDashboardController {

    private final AdminService adminService = new AdminService();

    @FXML private Label adminWelcomeLabel;
    @FXML private TabPane adminTabPane;
    @FXML private Button banButton;
    @FXML private Button unbanButton;

    @FXML private TableView<User> usersTableView;
    @FXML private TableColumn<User, Long> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, Boolean> userBannedColumn;

    @FXML private TableView<Ride> ridesTableView;
    @FXML private TableColumn<Ride, Long> rideIdColumn;
    @FXML private TableColumn<Ride, String> rideSourceColumn;
    @FXML private TableColumn<Ride, String> rideDestinationColumn;
    @FXML private TableColumn<Ride, String> rideStatusColumn;

    @FXML private TableView<AdminAuditLog> auditTableView;
    @FXML private TableColumn<AdminAuditLog, String> auditActionColumn;
    @FXML private TableColumn<AdminAuditLog, String> auditTargetColumn;
    @FXML private TableColumn<AdminAuditLog, String> auditNotesColumn;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            adminWelcomeLabel.setText("Admin Console [Logged in as: " + SessionManager.getCurrentUser().getName() + "]");
        }

        // Initialize table columns
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userBannedColumn.setCellValueFactory(new PropertyValueFactory<>("banned"));

        rideIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        rideSourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        rideDestinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
        rideStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        auditActionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        auditTargetColumn.setCellValueFactory(new PropertyValueFactory<>("targetType"));
        auditNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Load data on tab selection
        adminTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                String tabId = newTab.getId();
                if ("usersTab".equals(tabId)) {
                    loadUsers();
                } else if ("ridesTab".equals(tabId)) {
                    loadRides();
                } else if ("auditTab".equals(tabId)) {
                    loadAuditLog();
                }
            }
        });

        // Add selection listener to users table to show/hide ban/unban buttons
        usersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.isBanned()) {
                    banButton.setVisible(false);
                    banButton.setManaged(false);
                    unbanButton.setVisible(true);
                    unbanButton.setManaged(true);
                } else {
                    banButton.setVisible(true);
                    banButton.setManaged(true);
                    unbanButton.setVisible(false);
                    unbanButton.setManaged(false);
                }
            } else {
                banButton.setVisible(false);
                banButton.setManaged(false);
                unbanButton.setVisible(false);
                unbanButton.setManaged(false);
            }
        });
    }

    @FXML
    private void loadUsers() {
        new Thread(() -> {
            try {
                System.out.println("Loading users...");
                List<User> users = adminService.getAllUsers();
                System.out.println("Loaded " + users.size() + " users");
                Platform.runLater(() -> {
                    usersTableView.setItems(FXCollections.observableArrayList(users));
                    System.out.println("Users table updated with " + users.size() + " items");
                });
            } catch (Exception e) {
                System.err.println("Error loading users: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load users: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void loadRides() {
        new Thread(() -> {
            try {
                System.out.println("Loading rides...");
                List<Ride> rides = adminService.getAllRides();
                System.out.println("Loaded " + rides.size() + " rides");
                Platform.runLater(() -> {
                    ridesTableView.setItems(FXCollections.observableArrayList(rides));
                    System.out.println("Rides table updated with " + rides.size() + " items");
                });
            } catch (Exception e) {
                System.err.println("Error loading rides: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load rides: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void loadAuditLog() {
        new Thread(() -> {
            try {
                System.out.println("Loading audit log...");
                List<AdminAuditLog> auditLogs = adminService.getAuditLog();
                System.out.println("Loaded " + auditLogs.size() + " audit log entries");
                Platform.runLater(() -> {
                    auditTableView.setItems(FXCollections.observableArrayList(auditLogs));
                    System.out.println("Audit log table updated with " + auditLogs.size() + " items");
                });
            } catch (Exception e) {
                System.err.println("Error loading audit log: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load audit log: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void banSelectedUser() {
        User selected = usersTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a user to ban");
            return;
        }

        new Thread(() -> {
            try {
                adminService.banUser(selected.getId());
                Platform.runLater(() -> {
                    showAlert("Success", "User banned successfully");
                    loadUsers();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to ban user: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void unbanSelectedUser() {
        User selected = usersTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a user to unban");
            return;
        }

        new Thread(() -> {
            try {
                adminService.unbanUser(selected.getId());
                Platform.runLater(() -> {
                    showAlert("Success", "User unbanned successfully");
                    loadUsers();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to unban user: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void changeUserRole() {
        User selected = usersTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a user");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("RIDER", "DRIVER", "ADMIN");
        dialog.setTitle("Change User Role");
        dialog.setHeaderText("Select new role for " + selected.getName());
        dialog.setContentText("Role:");

        dialog.showAndWait().ifPresent(role -> {
            new Thread(() -> {
                try {
                    adminService.changeUserRole(selected.getId(), role);
                    Platform.runLater(() -> {
                        showAlert("Success", "Role changed successfully");
                        loadUsers();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to change role: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    @FXML
    private void cancelSelectedRide() {
        Ride selected = ridesTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a ride to cancel");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Ride Cancellation");
        confirm.setHeaderText("Are you sure you want to cancel this ride?");
        confirm.setContentText("Ride ID: " + selected.getId());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        adminService.cancelRide(selected.getId());
                        Platform.runLater(() -> {
                            showAlert("Success", "Ride cancelled successfully");
                            loadRides();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("Error", "Failed to cancel ride: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
