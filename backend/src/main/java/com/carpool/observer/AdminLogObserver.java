package com.carpool.observer;

import com.carpool.model.Ride;

public class AdminLogObserver implements RideStatusObserver {

    @Override
    public void onRideStatusChanged(Ride ride) {
        System.out.println("Admin log: Ride " + ride.getId()
                + " changed to " + ride.getStatus());
    }
}