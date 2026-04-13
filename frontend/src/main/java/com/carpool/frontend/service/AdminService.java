package com.carpool.frontend.service;

import com.carpool.frontend.model.User;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.AdminAuditLog;
import com.carpool.frontend.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * AdminService - Service for admin operations
 *
 * DESIGN PATTERNS USED:
 * 1. Service Layer Pattern - Encapsulates admin business logic
 *    - Acts as intermediary between controllers and backend API
 *    - Centralizes admin operations (user management, ride management, audit logs)
 *
 * 2. Repository Pattern (API-based) - Abstracts backend API calls
 *    - Decouples controller from HTTP implementation details
 *
 * DESIGN PRINCIPLES:
 * 1. Single Responsibility Principle (SRP)
 *    - Each method handles one specific admin operation
 *
 * 2. Dependency Inversion Principle (DIP)
 *    - Controller depends on this service abstraction
 */
public class AdminService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String BASE_URL = "http://localhost:8080/api/admin";

    public List<User> getAllUsers() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }

        String url = BASE_URL + "/users";
        System.out.println("Fetching users from: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        if (response.statusCode() == 200) {
            List<User> users = objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
            System.out.println("Parsed " + users.size() + " users");
            return users;
        } else {
            throw new Exception("Failed to fetch users: " + response.statusCode() + " - " + response.body());
        }
    }

    public User banUser(Long userId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/" + userId + "/ban"))
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("Failed to ban user: " + response.statusCode() + " - " + response.body());
        }
    }

    public User unbanUser(Long userId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/" + userId + "/unban"))
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("Failed to unban user: " + response.statusCode() + " - " + response.body());
        }
    }

    public User changeUserRole(Long userId, String role) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }

        String requestBody = "role=" + role;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/" + userId + "/role"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("Failed to change role: " + response.statusCode() + " - " + response.body());
        }
    }

    public List<Ride> getAllRides() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }

        String url = BASE_URL + "/rides";
        System.out.println("Fetching rides from: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Rides response status: " + response.statusCode());
        System.out.println("Rides response body: " + response.body());

        if (response.statusCode() == 200) {
            List<Ride> rides = objectMapper.readValue(response.body(), new TypeReference<List<Ride>>() {});
            System.out.println("Parsed " + rides.size() + " rides");
            return rides;
        } else {
            throw new Exception("Failed to fetch rides: " + response.statusCode() + " - " + response.body());
        }
    }

    public Ride cancelRide(Long rideId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/rides/" + rideId + "/cancel"))
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Ride.class);
        } else {
            throw new Exception("Failed to cancel ride: " + response.statusCode() + " - " + response.body());
        }
    }

    public List<AdminAuditLog> getAuditLog() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }

        String url = BASE_URL + "/audit";
        System.out.println("Fetching audit log from: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Audit log response status: " + response.statusCode());
        System.out.println("Audit log response body: " + response.body());

        if (response.statusCode() == 200) {
            List<AdminAuditLog> auditLogs = objectMapper.readValue(response.body(), new TypeReference<List<AdminAuditLog>>() {});
            System.out.println("Parsed " + auditLogs.size() + " audit log entries");
            return auditLogs;
        } else {
            throw new Exception("Failed to fetch audit log: " + response.statusCode() + " - " + response.body());
        }
    }
}
