package com.carpool.observer;

import com.carpool.model.Ride;

public interface RideStatusObserver {

    void onRideStatusChanged(Ride ride);

}