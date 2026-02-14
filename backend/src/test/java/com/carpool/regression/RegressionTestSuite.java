package com.carpool.regression;

import com.carpool.model.*;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegressionTestSuite {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void duplicateRegistrationShouldFail() {

        User user = new User("Dup",
                "dup@reg.com",
                "1234",
                "9999",
                UserRole.RIDER);

        restTemplate.postForEntity("/api/users/register",
                user, User.class);

        ResponseEntity<String> response =
                restTemplate.postForEntity("/api/users/register",
                        user, String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
    }

    @Test
    void overbookingShouldFail() {

        User driver = userRepository.save(
                new User("Driver2",
                        "driver2@reg.com",
                        "pass",
                        "111",
                        UserRole.DRIVER));

        User rider = userRepository.save(
                new User("Rider2",
                        "rider2@reg.com",
                        "pass",
                        "222",
                        UserRole.RIDER));

        Ride ride = new Ride("X", "Y",
                1, 100,
                LocalDateTime.now().plusDays(1),
                driver);

        ResponseEntity<Ride> rideResp =
                restTemplate.postForEntity(
                        "/api/rides/" + driver.getUserId(),
                        ride,
                        Ride.class);

        Long rideId = rideResp.getBody().getId();

        // First booking succeeds
        restTemplate.postForEntity(
                "/api/bookings?riderId="
                        + rider.getUserId()
                        + "&rideId="
                        + rideId
                        + "&seats=1",
                null,
                Booking.class);

        // Second booking should fail
        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/bookings?riderId="
                                + rider.getUserId()
                                + "&rideId="
                                + rideId
                                + "&seats=1",
                        null,
                        String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
    }
}
