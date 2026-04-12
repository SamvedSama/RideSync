package com.carpool.frontend.service;

import com.carpool.frontend.model.Booking;
import com.carpool.frontend.model.Ride;
import com.carpool.frontend.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HistoryService {
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String BASE_URL = "http://localhost:8080/api/history";
    
    public List<Booking> getPassengerRideHistory() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/passenger/rides"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Booking>>() {});
        } else {
            throw new Exception("Failed to fetch passenger ride history: " + response.statusCode());
        }
    }
    
    public List<Ride> getDriverRideHistory() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/driver/rides"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Ride>>() {});
        } else {
            throw new Exception("Failed to fetch driver ride history: " + response.statusCode());
        }
    }
    
    public List<Booking> getDriverBookingHistory() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/driver/bookings"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Booking>>() {});
        } else {
            throw new Exception("Failed to fetch driver booking history: " + response.statusCode());
        }
    }
    
    public double getDriverEarnings() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/driver/earnings"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return Double.parseDouble(response.body());
        } else {
            throw new Exception("Failed to fetch driver earnings: " + response.statusCode());
        }
    }
    
    public double getPassengerSpending() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/passenger/spending"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return Double.parseDouble(response.body());
        } else {
            throw new Exception("Failed to fetch passenger spending: " + response.statusCode());
        }
    }
}
