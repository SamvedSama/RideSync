package com.carpool.unit;

import com.carpool.service.strategy.FareStrategy;
import com.carpool.service.strategy.StandardFareStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class StandardFareStrategyTest {

    private final FareStrategy strategy = new StandardFareStrategy();

    @Test
    void testSingleSeat() {
        assertEquals(100.0, strategy.calculateTotalFare(100.0, 1), 0.001);
    }

    @Test
    void testMultipleSeats() {
        assertEquals(225.0, strategy.calculateTotalFare(75.0, 3), 0.001);
    }

    @Test
    void testFractionalFare() {
        assertEquals(99.98, strategy.calculateTotalFare(49.99, 2), 0.001);
    }

    @Test
    void testMaxSeats() {
        assertEquals(4000.0, strategy.calculateTotalFare(500.0, 8), 0.001);
    }

    @ParameterizedTest(name = "fare={0}, seats={1}, expected={2}")
    @CsvSource({
            "100.0, 1, 100.0",
            "50.0,  2, 100.0",
            "33.33, 3, 99.99",
            "200.0, 4, 800.0",
            "999.0, 1, 999.0"
    })
    void testParameterized(double farePerSeat, int seats, double expected) {
        assertEquals(expected, strategy.calculateTotalFare(farePerSeat, seats), 0.01);
    }

    @Test
    void testZeroFareThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculateTotalFare(0.0, 2));
    }

    @Test
    void testNegativeFareThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculateTotalFare(-10.0, 2));
    }
}