package com.carpool.frontend.service;

import com.carpool.frontend.model.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.carpool.frontend.util.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthService {

    private static final String BASE_URL = "http://localhost:8080/api/users";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public AuthResponse login(String email, String password) throws Exception {
        Map<String, String> creds = Map.of("email", email, "password", password);
        String requestBody = objectMapper.writeValueAsString(creds);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), AuthResponse.class);
        } else {
            throw new Exception("Login failed: " + response.body());
        }
    }

    public AuthResponse register(String name, String email, String password, String phone, String role) throws Exception {
        Map<String, String> payload = Map.of(
                "name", name,
                "email", email,
                "password", password,
                "phone", phone,
                "role", role
        );
        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), AuthResponse.class);
        } else {
            throw new Exception("Registration failed: " + response.body());
        }
    }

    public boolean updateProfile(Long userId, String name, String phone) throws Exception {
        String json = "{}"; // Put expects RequestParams on backend

        String urlString = BASE_URL + "/" + userId + "?name=" + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8) + "&phone=" + java.net.URLEncoder.encode(phone, java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to update profile: " + response.body());
        }
        return true;
    }

    public boolean hasActiveBookings() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/bookings/my-bookings"))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body().trim().length() > 5;
        } catch(Exception e) {
            return false;
        }
    }
}
