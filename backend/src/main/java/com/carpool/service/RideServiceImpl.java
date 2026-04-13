package com.carpool.service;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.User;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Ride Service Implementation
 * Follows Single Responsibility Principle
 */
@Service
@Transactional
public class RideServiceImpl implements RideService {
    
    @Autowired
    private RideRepository rideRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingService bookingService;
    
    @Override
    public Ride createRide(Long driverId, Ride ride) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        ride.setDriver(driver);
        ride.setStatus(RideStatus.PUBLISHED);
        if (ride.getDepartureTime() == null) {
            ride.setDepartureTime(LocalDateTime.now());
        }
        return rideRepository.save(ride);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Ride getRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        // Populate available seats
        ride.setAvailableSeats(ride.getAvailableSeats());
        return ride;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ride> getRidesByDriver(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        return rideRepository.findByDriverIdOrderByDepartureTimeDesc(driverId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ride> getAvailableRides() {
        List<Ride> rides = rideRepository.findByStatuses(List.of(RideStatus.PUBLISHED));
        // Populate available seats for each ride
        rides.forEach(ride -> ride.setAvailableSeats(ride.getAvailableSeats()));
        return rides;
    }
    
    @Override
    public Ride updateRideStatus(Long rideId, RideStatus status) {
        Optional<Ride> rideOpt = rideRepository.findById(rideId);
        if (rideOpt.isPresent()) {
            Ride ride = rideOpt.get();
            ride.setStatus(status);
            
            // Update booking statuses when ride status changes
            bookingService.updateBookingStatusesForRide(rideId, status);
            
            return rideRepository.save(ride);
        }
        throw new IllegalArgumentException("Ride not found");
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ride> searchRides(String source, String destination) {
        List<Ride> rides = rideRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndStatuses(
            source, destination, List.of(RideStatus.PUBLISHED)
        );
        // Populate available seats for each ride
        rides.forEach(ride -> ride.setAvailableSeats(ride.getAvailableSeats()));
        return rides;
    }
}
