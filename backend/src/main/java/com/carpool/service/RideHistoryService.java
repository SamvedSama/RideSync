package com.carpool.service;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.Booking;
import com.carpool.model.BookingStatus;
import com.carpool.model.PaymentStatus;
import com.carpool.model.User;
import com.carpool.repository.RideRepository;
import com.carpool.repository.BookingRepository;
import com.carpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ride History Service for analytics and statistics
 * Follows Single Responsibility Principle
 */
@Service
@Transactional
public class RideHistoryService {
    
    @Autowired
    private RideRepository rideRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get user's ride history as passenger
     */
    @Transactional(readOnly = true)
    public List<Ride> getPassengerRideHistory(Long passengerId) {
        List<Booking> bookings = bookingRepository.findByRiderOrderByCreatedAtDesc(
            userRepository.findById(passengerId).orElseThrow()
        );
        return bookings.stream()
            .map(Booking::getRide)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * Get user's ride history as driver
     */
    @Transactional(readOnly = true)
    public List<Ride> getDriverRideHistory(Long driverId) {
        return rideRepository.findByDriverIdOrderByDepartureTimeDesc(driverId);
    }
    
    /**
     * Get completed rides for user
     */
    @Transactional(readOnly = true)
    public List<Ride> getCompletedRides(Long userId) {
        List<Booking> bookings = bookingRepository.findByRiderAndStatusOrderByCreatedAtDesc(
            userRepository.findById(userId).orElseThrow(),
            BookingStatus.COMPLETED
        );
        return bookings.stream()
            .map(Booking::getRide)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        List<Booking> bookings = bookingRepository.findByRiderId(userId);
        List<Ride> rides = rideRepository.findByDriverIdOrderByDepartureTimeDesc(userId);
        
        double totalSpent = bookings.stream()
            .filter(b -> b.getPayment() != null && b.getPayment().getStatus() == PaymentStatus.COMPLETED)
            .mapToDouble(b -> b.getPayment().getAmount())
            .sum();
        
        double totalEarnings = rides.stream()
            .filter(r -> r.getStatus() == RideStatus.COMPLETED)
            .mapToDouble(r -> r.getFarePerSeat() * r.getTotalSeats())
            .sum();
        
        return new UserStatistics(
            user.getRating(),
            bookings.size(),
            rides.size(),
            totalSpent,
            totalEarnings
        );
    }
    
    /**
     * Get system statistics
     */
    @Transactional(readOnly = true)
    public SystemStatistics getSystemStatistics() {
        long totalUsers = userRepository.count();
        long passengers = userRepository.countByRole(com.carpool.model.UserRole.PASSENGER);
        long drivers = userRepository.countByRole(com.carpool.model.UserRole.DRIVER);
        long totalRides = rideRepository.count();
        long completedRides = rideRepository.countByStatus(RideStatus.COMPLETED);
        long totalBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        
        return new SystemStatistics(
            totalUsers,
            passengers,
            drivers,
            totalRides,
            completedRides,
            totalBookings
        );
    }
    
    /**
     * Get popular routes
     */
    @Transactional(readOnly = true)
    public List<RouteStatistics> getPopularRoutes() {
        List<Object[]> results = rideRepository.findPopularRoutes();
        return results.stream()
            .map(result -> new RouteStatistics(
                (String) result[0],
                (String) result[1],
                ((Number) result[2]).longValue()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Get recent rides
     */
    @Transactional(readOnly = true)
    public List<Ride> getRecentRides(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return rideRepository.findTop10ByOrderByDepartureTimeDesc(pageable);
    }
    
    /**
     * Get monthly statistics for user
     */
    @Transactional(readOnly = true)
    public List<MonthlyStatistics> getMonthlyStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findByRiderIdAndCreatedAtBetween(userId, startDate, endDate);
        
        Map<String, Long> monthlyCounts = bookings.stream()
            .collect(Collectors.groupingBy(
                b -> b.getCreatedAt().getMonth().toString(),
                Collectors.counting()
            ));
        
        return monthlyCounts.entrySet().stream()
            .map(entry -> new MonthlyStatistics(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(MonthlyStatistics::getMonth))
            .collect(Collectors.toList());
    }
    
    // Data classes for statistics
    public static class UserStatistics {
        private final double rating;
        private final int totalBookings;
        private final int totalRides;
        private final double totalSpent;
        private final double totalEarnings;
        
        public UserStatistics(double rating, int totalBookings, int totalRides, double totalSpent, double totalEarnings) {
            this.rating = rating;
            this.totalBookings = totalBookings;
            this.totalRides = totalRides;
            this.totalSpent = totalSpent;
            this.totalEarnings = totalEarnings;
        }
        
        // Getters
        public double getRating() { return rating; }
        public int getTotalBookings() { return totalBookings; }
        public int getTotalRides() { return totalRides; }
        public double getTotalSpent() { return totalSpent; }
        public double getTotalEarnings() { return totalEarnings; }
    }
    
    public static class SystemStatistics {
        private final long totalUsers;
        private final long passengers;
        private final long drivers;
        private final long totalRides;
        private final long completedRides;
        private final long totalBookings;
        
        public SystemStatistics(long totalUsers, long passengers, long drivers, long totalRides, long completedRides, long totalBookings) {
            this.totalUsers = totalUsers;
            this.passengers = passengers;
            this.drivers = drivers;
            this.totalRides = totalRides;
            this.completedRides = completedRides;
            this.totalBookings = totalBookings;
        }
        
        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getPassengers() { return passengers; }
        public long getDrivers() { return drivers; }
        public long getTotalRides() { return totalRides; }
        public long getCompletedRides() { return completedRides; }
        public long getTotalBookings() { return totalBookings; }
    }
    
    public static class RouteStatistics {
        private final String source;
        private final String destination;
        private final long count;
        
        public RouteStatistics(String source, String destination, long count) {
            this.source = source;
            this.destination = destination;
            this.count = count;
        }
        
        // Getters
        public String getSource() { return source; }
        public String getDestination() { return destination; }
        public long getCount() { return count; }
    }
    
    public static class MonthlyStatistics {
        private final String month;
        private final long count;
        
        public MonthlyStatistics(String month, long count) {
            this.month = month;
            this.count = count;
        }
        
        // Getters
        public String getMonth() { return month; }
        public long getCount() { return count; }
    }
}
