package com.carpool.observer;

import com.carpool.model.Ride;

public class DriverNotificationObserver implements RideStatusObserver {

    @Override
    public void onRideStatusChanged(Ride ride) {
        System.out.println("Driver notified: Ride status updated to "
                + ride.getStatus());
    }
}