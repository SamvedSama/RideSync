package com.carpool.frontend.service;

import com.carpool.frontend.model.Ride;
import com.carpool.frontend.model.RideStatus;
import com.carpool.frontend.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RideService {

    private static final String BASE_URL = "http://localhost:8080/api/rides";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RideService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Ride createRide(Ride ride) throws Exception {
        String requestBody = objectMapper.writeValueAsString(ride);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Ride.class);
        } else {
            throw new Exception("Error creating ride: " + response.body());
        }
    }

    public List<Ride> searchRides(String source, String destination) throws Exception {
        String uri = BASE_URL + "/search";
        if (source != null && !source.isBlank() && destination != null && !destination.isBlank()) {
            uri += "?source=" + java.net.URLEncoder.encode(source, "UTF-8") + "&destination=" + java.net.URLEncoder.encode(destination, "UTF-8");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
             return objectMapper.readValue(response.body(), new TypeReference<List<Ride>>() {});
        } else {
            throw new Exception("Error searching rides: " + response.body());
        }
    }

    public List<Ride> getMyRides() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/my-rides"))
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
             return objectMapper.readValue(response.body(), new TypeReference<List<Ride>>() {});
        } else {
            throw new Exception("Error fetching driver rides: " + response.body());
        }
    }

    public Ride updateRideStatus(Long rideId, RideStatus status) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + rideId + "/status?status=" + status))
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Ride.class);
        } else {
            throw new Exception("Error updating ride status: " + response.body());
        }
    }

    public Ride getRide(Long rideId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + rideId))
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Ride.class);
        } else {
            throw new Exception("Error fetching ride: " + response.body());
        }
    }
    
    public Ride getRideById(Long rideId) throws Exception {
        return getRide(rideId);
    }
    
    public List<com.carpool.frontend.model.Booking> getRideBookings(Long rideId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + rideId + "/bookings"))
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<com.carpool.frontend.model.Booking>>() {});
        } else {
            throw new Exception("Error fetching ride bookings: " + response.body());
        }
    }
    
    public List<Ride> getBookedRides() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/booked"))
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Ride>>() {});
        } else {
            throw new Exception("Error fetching booked rides: " + response.body());
        }
    }
    
    public void startRide(Long rideId) throws Exception {
        updateRideStatus(rideId, RideStatus.IN_PROGRESS);
    }
    
    public void completeRide(Long rideId) throws Exception {
        updateRideStatus(rideId, RideStatus.COMPLETED);
    }
    
    public void shareTripDetails(Long rideId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + rideId + "/share"))
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Error sharing trip details: " + response.body());
        }
    }
}
