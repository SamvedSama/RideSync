package com.carpool.observer;

import com.carpool.model.Ride;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RideStatusManager {

    private final List<RideStatusObserver> observers = new ArrayList<>();

    public void addObserver(RideStatusObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(Ride ride) {
        for (RideStatusObserver observer : observers) {
            observer.onRideStatusChanged(ride);
        }
    }
}