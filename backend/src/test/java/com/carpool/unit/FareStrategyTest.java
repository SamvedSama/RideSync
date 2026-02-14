package com.carpool.unit;

import com.carpool.service.strategy.StandardFareStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FareStrategyTest {

    @Test
    void testFareCalculation() {
        StandardFareStrategy strategy = new StandardFareStrategy();
        double total = strategy.calculateTotalFare(100, 3);
        assertEquals(300, total);
    }

    @Test
    void testInvalidFare() {
        StandardFareStrategy strategy = new StandardFareStrategy();
        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculateTotalFare(-1, 2));
    }
}
