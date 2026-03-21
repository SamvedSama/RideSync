package com.carpool.unit;

import com.carpool.service.strategy.FareStrategy;
import com.carpool.service.strategy.StandardFareStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FareStrategyTest {

    private final FareStrategy strategy = new StandardFareStrategy();

    @Test
    void testCalculateTotalFare_singleSeat() {
        assertEquals(100.0, strategy.calculateTotalFare(100.0, 1), 0.001);
    }

    @Test
    void testCalculateTotalFare_multipleSeats() {
        assertEquals(225.0, strategy.calculateTotalFare(75.0, 3), 0.001);
    }

    @Test
    void testCalculateTotalFare_fractionalFare() {
        assertEquals(99.98, strategy.calculateTotalFare(49.99, 2), 0.001);
    }

    @Test
    void testCalculateTotalFare_largeValues() {
        assertEquals(4000.0, strategy.calculateTotalFare(500.0, 8), 0.001);
    }
}