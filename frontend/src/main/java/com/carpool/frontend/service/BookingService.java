package com.carpool.frontend.service;

import com.carpool.frontend.model.Booking;
import com.carpool.frontend.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class BookingService {
    private static final String API_URL = "http://localhost:8080/api/bookings";
    private final HttpClient client;
    private final ObjectMapper mapper;
    
    public BookingService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }
    
    public Booking bookRide(Long rideId, int seats) throws Exception {
        String token = SessionManager.getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?rideId=" + rideId + "&seats=" + seats))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
                
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), Booking.class);
        }
        throw new RuntimeException("Failed to book ride: " + response.body());
    }
    
    public List<Booking> getMyBookings() throws Exception {
        String token = SessionManager.getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/my-bookings"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
                
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), new TypeReference<List<Booking>>(){});
        }
        throw new RuntimeException("Failed to fetch bookings: " + response.body());
    }
    
    public Booking startRideWithOtp(Long rideId, String otp) throws Exception {
        String token = SessionManager.getToken();
        
        Map<String, String> payloadMap = Map.of("otp", otp);
        String payload = mapper.writeValueAsString(payloadMap);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/ride/" + rideId + "/start"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
                
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), Booking.class);
        }
        throw new RuntimeException("Failed to start ride with OTP: " + response.body());
    }
}
