package com.carpool.algorithm;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dijkstra's Algorithm implementation for finding shortest paths
 * Behavioral Design Pattern: Strategy (concrete implementation)
 * Follows Single Responsibility and Open/Closed Principles
 */
@Component
public class DijkstraRoutingAlgorithm implements RoutingAlgorithm {
    
    private final Map<String, Map<String, Double>> graph;
    private static final double FARE_PER_KM = 2.5; // $2.50 per km
    private static final double AVG_SPEED_KMH = 40.0; // Average city speed
    
    public DijkstraRoutingAlgorithm() {
        this.graph = new HashMap<>();
        initializeCollegeCampusGraph();
    }
    
    /**
     * Initialize a sample college campus graph with locations and distances
     */
    private void initializeCollegeCampusGraph() {
        // Main campus locations
        addBidirectionalEdge("Main Gate", "Library", 0.5);
        addBidirectionalEdge("Main Gate", "Admin Block", 0.3);
        addBidirectionalEdge("Main Gate", "Cafeteria", 0.4);
        
        addBidirectionalEdge("Library", "Computer Science Dept", 0.6);
        addBidirectionalEdge("Library", "Engineering Block", 0.8);
        addBidirectionalEdge("Library", "Cafeteria", 0.3);
        
        addBidirectionalEdge("Admin Block", "Engineering Block", 0.4);
        addBidirectionalEdge("Admin Block", "Student Center", 0.5);
        
        addBidirectionalEdge("Cafeteria", "Student Center", 0.2);
        addBidirectionalEdge("Cafeteria", "Sports Complex", 0.7);
        
        addBidirectionalEdge("Computer Science Dept", "Engineering Block", 0.3);
        addBidirectionalEdge("Computer Science Dept", "Research Lab", 0.5);
        
        addBidirectionalEdge("Engineering Block", "Research Lab", 0.4);
        addBidirectionalEdge("Engineering Block", "Workshop", 0.6);
        
        addBidirectionalEdge("Student Center", "Hostel A", 1.2);
        addBidirectionalEdge("Student Center", "Hostel B", 1.0);
        
        addBidirectionalEdge("Sports Complex", "Hostel A", 0.8);
        addBidirectionalEdge("Sports Complex", "Hostel B", 0.9);
        
        addBidirectionalEdge("Hostel A", "Hostel B", 0.5);
        addBidirectionalEdge("Hostel A", "Bus Stop", 0.6);
        addBidirectionalEdge("Hostel B", "Bus Stop", 0.4);
        
        addBidirectionalEdge("Bus Stop", "Metro Station", 1.5);
        addBidirectionalEdge("Bus Stop", "Shopping Mall", 2.0);
        
        addBidirectionalEdge("Metro Station", "Shopping Mall", 1.8);
        addBidirectionalEdge("Metro Station", "City Center", 2.5);
        
        addBidirectionalEdge("Shopping Mall", "City Center", 1.2);
        addBidirectionalEdge("Shopping Mall", "Airport", 8.0);
        
        addBidirectionalEdge("City Center", "Airport", 7.5);
        addBidirectionalEdge("City Center", "Railway Station", 3.0);
        
        addBidirectionalEdge("Airport", "Railway Station", 6.5);
    }
    
    private void addBidirectionalEdge(String from, String to, double distance) {
        graph.computeIfAbsent(from, k -> new HashMap<>()).put(to, distance);
        graph.computeIfAbsent(to, k -> new HashMap<>()).put(from, distance);
    }
    
    @Override
    public RouteResult findShortestPath(String source, String destination) {
        if (!graph.containsKey(source) || !graph.containsKey(destination)) {
            throw new IllegalArgumentException("Source or destination not found in the graph");
        }
        
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        
        // Initialize distances
        for (String node : graph.keySet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);
        pq.add(source);
        
        while (!pq.isEmpty()) {
            String current = pq.poll();
            
            if (current.equals(destination)) {
                break;
            }
            
            for (Map.Entry<String, Double> neighbor : graph.get(current).entrySet()) {
                String neighborNode = neighbor.getKey();
                double edgeWeight = neighbor.getValue();
                double newDistance = distances.get(current) + edgeWeight;
                
                if (newDistance < distances.get(neighborNode)) {
                    distances.put(neighborNode, newDistance);
                    previousNodes.put(neighborNode, current);
                    pq.add(neighborNode);
                }
            }
        }
        
        // Reconstruct path
        List<String> path = new ArrayList<>();
        String current = destination;
        while (current != null) {
            path.add(0, current);
            current = previousNodes.get(current);
        }
        
        double totalDistance = distances.get(destination);
        long estimatedTime = calculateEstimatedTimeFromDistance(totalDistance);
        double estimatedFare = calculateFareFromDistance(totalDistance);
        
        return new RouteResult(path, totalDistance, estimatedTime, estimatedFare);
    }
    
    @Override
    public double calculateDistance(String source, String destination) {
        return findShortestPath(source, destination).getTotalDistance();
    }
    
    @Override
    public long calculateEstimatedTime(String source, String destination) {
        return findShortestPath(source, destination).getEstimatedTimeMinutes();
    }
    
    private long calculateEstimatedTimeFromDistance(double distance) {
        return Math.round((distance / AVG_SPEED_KMH) * 60); // Convert to minutes
    }
    
    private double calculateFareFromDistance(double distance) {
        return Math.round(distance * FARE_PER_KM * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    /**
     * Get all available locations in the graph
     */
    public Set<String> getAllLocations() {
        return new HashSet<>(graph.keySet());
    }
    
    /**
     * Add a new location to the graph
     */
    public void addLocation(String location) {
        graph.putIfAbsent(location, new HashMap<>());
    }
    
    /**
     * Add a connection between two locations
     */
    public void addConnection(String from, String to, double distance) {
        addBidirectionalEdge(from, to, distance);
    }
}
