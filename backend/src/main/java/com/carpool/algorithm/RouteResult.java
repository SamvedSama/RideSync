package com.carpool.algorithm;

import java.util.List;

/**
 * Data class to hold routing results
 * Follows Single Responsibility Principle
 */
public class RouteResult {
    private final List<String> path;
    private final double totalDistance;
    private final long estimatedTimeMinutes;
    private final double estimatedFare;
    
    public RouteResult(List<String> path, double totalDistance, long estimatedTimeMinutes, double estimatedFare) {
        this.path = path;
        this.totalDistance = totalDistance;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.estimatedFare = estimatedFare;
    }
    
    public List<String> getPath() {
        return path;
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public long getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }
    
    public double getEstimatedFare() {
        return estimatedFare;
    }
    
    @Override
    public String toString() {
        return String.format("Route: %s, Distance: %.2f km, Time: %d min, Fare: $%.2f", 
                String.join(" -> ", path), totalDistance, estimatedTimeMinutes, estimatedFare);
    }
}
