package com.carpool.frontend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LocationService {

    private final HttpClient client;

    public LocationService() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Connects to OpenStreetMap's Nominatim API to ensure a city/street name exists.
     */
    public boolean isValidLocation(String query) {
        if (query == null || query.trim().isEmpty()) return false;
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&countrycodes=in&limit=1"))
                    .header("User-Agent", "RideSync-JavaFX-App")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body().trim().length() > 5;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Connects to OpenStreetMap's Nominatim API to fetch location suggestions.
     */
    public java.util.List<String> fetchSuggestions(String query) {
        if (query == null || query.trim().length() < 3) return java.util.Collections.emptyList();
        try {
            String encodedQuery = java.net.URLEncoder.encode(query + " Bengaluru Karnataka", "UTF-8");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&countrycodes=in&limit=5"))
                    .header("User-Agent", "RideSync-JavaFX-App")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return java.util.Collections.emptyList();

            java.util.List<String> suggestions = new java.util.ArrayList<>();
            // Very simple JSON parsing using string matching to avoid full ObjectMapper setup overhead here,
            // though Jackson is available.
            String body = response.body();
            String[] parts = body.split("\"display_name\":\"");
            for (int i = 1; i < parts.length; i++) {
                String name = parts[i].substring(0, parts[i].indexOf("\""));
                // Decode unicode escapes if any
                suggestions.add(name);
            }
            return suggestions;
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
    public double[] geocode(String place) {
        if (place == null || place.trim().isEmpty()) return new double[]{12.9716, 77.5946}; // Default Bengaluru
        try {
            String encodedQuery = java.net.URLEncoder.encode(place + " Bengaluru Karnataka", "UTF-8");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&countrycodes=in&limit=1"))
                    .header("User-Agent", "RideSync-JavaFX-App")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            int latIdx = body.indexOf("\"lat\":\"");
            int lonIdx = body.indexOf("\"lon\":\"");
            if (latIdx != -1 && lonIdx != -1) {
                String latStr = body.substring(latIdx + 7, body.indexOf("\"", latIdx + 7));
                String lonStr = body.substring(lonIdx + 7, body.indexOf("\"", lonIdx + 7));
                return new double[]{Double.parseDouble(latStr), Double.parseDouble(lonStr)};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{12.9716, 77.5946};
    }
}
