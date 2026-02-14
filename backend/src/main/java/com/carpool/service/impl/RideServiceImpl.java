package com.carpool.service.impl;

import com.carpool.model.*;
import com.carpool.repository.*;
import com.carpool.service.RideService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    public RideServiceImpl(RideRepository rideRepository,
                           UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Ride createRide(Long driverId, Ride ride) {

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new RuntimeException("Only drivers can create rides");
        }

        ride.setDriver(driver);
        ride.setStatus(RideStatus.PUBLISHED);

        return rideRepository.save(ride);
    }

    @Override
    public List<Ride> searchRides(String source, String destination) {
        return rideRepository
                .findBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination);
    }
public class InnerRideServiceImpl {

    
}
    @Override
    public Ride getRide(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
    }
}
