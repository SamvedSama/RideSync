package com.carpool.observer;

import com.carpool.model.Ride;

public class RiderNotificationObserver implements RideStatusObserver {

    @Override
    public void onRideStatusChanged(Ride ride) {
        System.out.println("Riders notified: Ride " + ride.getId()
                + " status changed to " + ride.getStatus());
    }
}