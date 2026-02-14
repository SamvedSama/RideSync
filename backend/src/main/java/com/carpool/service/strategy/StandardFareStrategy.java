package com.carpool.service.strategy;

import org.springframework.stereotype.Component;

@Component
public class StandardFareStrategy implements FareStrategy {

    @Override
    public double calculateTotalFare(double farePerSeat, int seats) {
        if (farePerSeat <= 0 || seats <= 0) {
            throw new IllegalArgumentException("Fare and seats must be positive");
        }
        return farePerSeat * seats;
    }
}
