package com.carpool.service;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;

import java.util.List;

public interface RideService {
    Ride createRide(Long driverId, Ride ride);
    List<Ride> searchRides(String source, String destination);
    Ride getRide(Long rideId);
    Ride updateRideStatus(Long rideId, RideStatus status);
    List<Ride> getRidesByDriver(Long driverId);
    List<Ride> getAllRides();
}
