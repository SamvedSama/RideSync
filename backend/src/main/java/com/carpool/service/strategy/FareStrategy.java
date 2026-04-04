package com.carpool.service.strategy;

public interface FareStrategy {
    double calculateTotalFare(double farePerSeat, int seats);
    double calculateTotalFare(double farePerSeat, int seats, double waitingCharge);
}