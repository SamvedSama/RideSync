package com.carpool.controller;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.Booking;
import com.carpool.model.User;
import com.carpool.service.RideService;
import com.carpool.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private RideService rideService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/driver/rides")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Ride>> getDriverRideHistory(@AuthenticationPrincipal User currentUser) {
        try {
            // Get completed rides for the driver
            List<Ride> allRides = rideService.getRidesByDriver(currentUser.getUserId());
            List<Ride> completedRides = allRides.stream()
                    .filter(ride -> ride.getStatus() == RideStatus.COMPLETED)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(completedRides);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver ride history: " + e.getMessage());
        }
    }

    @GetMapping("/driver/bookings")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Booking>> getDriverBookingHistory(@AuthenticationPrincipal User currentUser) {
        try {
            // Get all bookings for driver's completed rides
            List<Ride> completedRides = rideService.getRidesByDriver(currentUser.getUserId()).stream()
                    .filter(ride -> ride.getStatus() == RideStatus.COMPLETED)
                    .collect(Collectors.toList());
            
            List<Booking> allBookings = bookingService.getBookingsForRides(completedRides);
            return ResponseEntity.ok(allBookings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver booking history: " + e.getMessage());
        }
    }

    @GetMapping("/passenger/rides")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('RIDER')")
    public ResponseEntity<List<Booking>> getPassengerRideHistory(@AuthenticationPrincipal User currentUser) {
        try {
            // Get all bookings for the passenger
            List<Booking> allBookings = bookingService.getBookingsByUser(currentUser.getUserId());
            
            // Filter for completed rides
            List<Booking> completedBookings = allBookings.stream()
                    .filter(booking -> booking.getRide().getStatus() == RideStatus.COMPLETED)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(completedBookings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get passenger ride history: " + e.getMessage());
        }
    }

    @GetMapping("/driver/earnings")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Double> getDriverEarnings(@AuthenticationPrincipal User currentUser) {
        try {
            List<Ride> completedRides = rideService.getRidesByDriver(currentUser.getUserId()).stream()
                    .filter(ride -> ride.getStatus() == RideStatus.COMPLETED)
                    .collect(Collectors.toList());
            
            double totalEarnings = 0.0;
            for (Ride ride : completedRides) {
                List<Booking> bookings = bookingService.getBookingsForRide(ride.getId());
                totalEarnings += bookings.stream()
                        .mapToDouble(booking -> booking.getTotalFare())
                        .sum();
            }
            
            return ResponseEntity.ok(totalEarnings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver earnings: " + e.getMessage());
        }
    }

    @GetMapping("/passenger/spending")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('RIDER')")
    public ResponseEntity<Double> getPassengerSpending(@AuthenticationPrincipal User currentUser) {
        try {
            List<Booking> completedBookings = bookingService.getBookingsByUser(currentUser.getUserId()).stream()
                    .filter(booking -> booking.getRide().getStatus() == RideStatus.COMPLETED)
                    .collect(Collectors.toList());
            
            double totalSpending = completedBookings.stream()
                    .mapToDouble(Booking::getTotalFare)
                    .sum();
            
            return ResponseEntity.ok(totalSpending);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get passenger spending: " + e.getMessage());
        }
    }
}
