package com.carpool.service.strategy;

import org.springframework.stereotype.Component;

@Component
public class StandardFareStrategy implements FareStrategy {

    @Override
    public double calculateTotalFare(double farePerSeat, int seats) {
        return calculateTotalFare(farePerSeat, seats, 0.0);
    }

    @Override
    public double calculateTotalFare(double farePerSeat, int seats, double waitingCharge) {
        if (farePerSeat <= 0 || seats <= 0) {
            throw new IllegalArgumentException("Fare and seats must be positive");
        }
        return (farePerSeat * seats) + Math.max(waitingCharge, 0.0);
    }
}