package com.carpool.service.impl;

import com.carpool.model.*;
import com.carpool.repository.*;
import com.carpool.service.RideService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpool.observer.RideStatusManager;
import com.carpool.observer.RiderNotificationObserver;
import com.carpool.observer.DriverNotificationObserver;
import com.carpool.observer.AdminLogObserver;

import java.util.List;

@Service
@Transactional
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    private final RideStatusManager statusManager = new RideStatusManager();

    public RideServiceImpl(RideRepository rideRepository,
            UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;

        statusManager.addObserver(new RiderNotificationObserver());
        statusManager.addObserver(new DriverNotificationObserver());
        statusManager.addObserver(new AdminLogObserver());
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

        Ride saved = rideRepository.save(ride);
        saved.setAvailableSeats(saved.getTotalSeats());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> searchRides(String source, String destination) {
        List<Ride> rides = rideRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndStatuses(
                source,
                destination,
                List.of(RideStatus.PUBLISHED, RideStatus.BOOKED)
        );
        rides.forEach(r -> r.setAvailableSeats(r.getAvailableSeats()));
        return rides;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> getAvailableRides() {
        List<Ride> rides = rideRepository.findByStatuses(List.of(RideStatus.PUBLISHED, RideStatus.BOOKED));
        rides.forEach(r -> r.setAvailableSeats(r.getAvailableSeats()));
        return rides;
    }

    @Override
    @Transactional(readOnly = true)
    public Ride getRide(Long rideId) {
        Ride ride = rideRepository.findByIdWithBookings(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        ride.setAvailableSeats(ride.getAvailableSeats());
        return ride;
    }

    @Override
    public Ride updateRideStatus(Long rideId, RideStatus status) {
        Ride ride = rideRepository.findByIdWithBookings(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalArgumentException("Completed ride cannot be modified");
        }

        if (ride.getStatus() == RideStatus.IN_PROGRESS &&
                (status == RideStatus.PUBLISHED || status == RideStatus.BOOKED)) {
            throw new IllegalArgumentException("In-progress ride cannot move backwards");
        }

        ride.setStatus(status);
        Ride updatedRide = rideRepository.save(ride);
        updatedRide.setAvailableSeats(updatedRide.getAvailableSeats());
        statusManager.notifyObservers(updatedRide);
        return updatedRide;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> getRidesByDriver(Long driverId) {
        List<Ride> rides = rideRepository.findByDriverUserId(driverId);
        rides.forEach(r -> r.setAvailableSeats(r.getAvailableSeats()));
        return rides;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> getAllRides() {
        List<Ride> rides = rideRepository.findAll();
        rides.forEach(r -> r.setAvailableSeats(r.getAvailableSeats()));
        return rides;
    }
}