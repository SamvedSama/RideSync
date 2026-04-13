package com.carpool.frontend.service;

import com.carpool.frontend.model.Payment;
import com.carpool.frontend.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PaymentService {
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String BASE_URL = "http://localhost:8080/api/payments";
    
    public Payment processPayment(Long bookingId, double amount, String paymentMethod) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        String requestBody = String.format("bookingId=%d&amount=%.2f&paymentMethod=%s", bookingId, amount, paymentMethod);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/process"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Payment.class);
        } else {
            throw new Exception("Failed to process payment: " + response.statusCode());
        }
    }
    
    public Payment processPassengerPayment(Long bookingId, String paymentMethod, Long passengerId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        String requestBody = String.format("bookingId=%d&paymentMethod=%s", bookingId, paymentMethod);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/passenger/pay"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Payment.class);
        } else {
            throw new Exception("Failed to process passenger payment: " + response.statusCode() + " - " + response.body());
        }
    }
    
    public Payment getPaymentByBooking(Long bookingId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/booking/" + bookingId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Payment.class);
        } else if (response.statusCode() == 404) {
            return null; // Payment not found
        } else {
            throw new Exception("Failed to fetch payment: " + response.statusCode());
        }
    }
    
    public Payment completePayment(Long paymentId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + paymentId + "/complete"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Payment.class);
        } else {
            throw new Exception("Failed to complete payment: " + response.statusCode());
        }
    }
    
    public List<Payment> getMyPayments() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/my-payments"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Payment>>() {});
        } else {
            throw new Exception("Failed to fetch payments: " + response.statusCode());
        }
    }
    
    public List<Payment> getPaymentsForRide(Long rideId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/driver/ride/" + rideId + "/payments"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Payment>>() {});
        } else {
            throw new Exception("Failed to fetch ride payments: " + response.statusCode());
        }
    }
    
    public List<Payment> getPendingPayments() throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/pending"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Payment>>() {});
        } else {
            throw new Exception("Failed to fetch pending payments: " + response.statusCode());
        }
    }
    
    public Payment confirmPayment(Long paymentId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/confirm/" + paymentId))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Payment.class);
        } else {
            throw new Exception("Failed to confirm payment: " + response.statusCode());
        }
    }
    
    public Payment rejectPayment(Long paymentId) throws Exception {
        String token = SessionManager.getToken();
        if (token == null) {
            throw new Exception("Not authenticated");
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reject/" + paymentId))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Payment.class);
        } else {
            throw new Exception("Failed to reject payment: " + response.statusCode());
        }
    }
}
