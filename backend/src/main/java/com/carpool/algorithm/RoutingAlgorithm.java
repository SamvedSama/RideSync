package com.carpool.algorithm;

/**
 * Strategy interface for routing algorithms
 * Behavioral Design Pattern: Strategy
 */
public interface RoutingAlgorithm {
    RouteResult findShortestPath(String source, String destination);
    double calculateDistance(String source, String destination);
    long calculateEstimatedTime(String source, String destination);
}
