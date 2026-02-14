package com.carpool.system;

import com.carpool.dto.RegisterRequest;
import com.carpool.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SystemFlowTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void fullApplicationFlow() {

                // Register Driver
                RegisterRequest driverRequest = new RegisterRequest(
                                "Driver",
                                "driver@sys.com",
                                "pass",
                                "1111",
                                UserRole.DRIVER);

                ResponseEntity<User> driverResp = restTemplate.postForEntity(
                                "/api/users/register",
                                driverRequest,
                                User.class);

                assertEquals(HttpStatus.OK, driverResp.getStatusCode());
                Long driverId = driverResp.getBody().getUserId();

                // Register Rider
                RegisterRequest riderRequest = new RegisterRequest(
                                "Rider",
                                "rider@sys.com",
                                "pass",
                                "2222",
                                UserRole.RIDER);

                ResponseEntity<User> riderResp = restTemplate.postForEntity(
                                "/api/users/register",
                                riderRequest,
                                User.class);

                assertEquals(HttpStatus.OK, riderResp.getStatusCode());
                Long riderId = riderResp.getBody().getUserId();

                // Driver creates ride
                Ride ride = new Ride(
                                "CityA",
                                "CityB",
                                2,
                                100,
                                LocalDateTime.now().plusDays(1),
                                driverResp.getBody());

                ResponseEntity<Ride> rideResp = restTemplate.postForEntity(
                                "/api/rides/" + driverId,
                                ride,
                                Ride.class);

                assertEquals(HttpStatus.OK, rideResp.getStatusCode());
                Long rideId = rideResp.getBody().getId();

                // Rider books ride
                ResponseEntity<Booking> bookingResp = restTemplate.postForEntity(
                                "/api/bookings?riderId="
                                                + riderId
                                                + "&rideId="
                                                + rideId
                                                + "&seats=2",
                                null,
                                Booking.class);

                assertEquals(HttpStatus.OK, bookingResp.getStatusCode());

                // Cancel booking
                Long bookingId = bookingResp.getBody().getId();

                ResponseEntity<Booking> cancelResp = restTemplate.exchange(
                                "/api/bookings/" + bookingId + "/cancel",
                                HttpMethod.PUT,
                                null,
                                Booking.class);

                assertEquals(HttpStatus.OK, cancelResp.getStatusCode());
                assertEquals(BookingStatus.CANCELLED,
                                cancelResp.getBody().getStatus());
        }
}
