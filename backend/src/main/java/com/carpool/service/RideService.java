package com.carpool.service;

import com.carpool.model.Ride;
import java.util.List;

public interface RideService {

    Ride createRide(Long driverId, Ride ride);

    List<Ride> searchRides(String source, String destination);

    Ride getRide(Long rideId);
}
