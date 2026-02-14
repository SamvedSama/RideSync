package com.carpool.integration;

import com.carpool.model.*;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private UserRepository userRepository;

        @Test
        void testBookingFlow() {

                User driver = new User("Driver", "driver@mail.com", "1234", "999", UserRole.DRIVER);
                driver = userRepository.save(driver);

                User rider = new User("Rider", "rider@mail.com", "1234", "888", UserRole.RIDER);
                rider = userRepository.save(rider);

                Ride ride = new Ride("CityA", "CityB", 3, 100,
                                LocalDateTime.now().plusDays(1), driver);

                ResponseEntity<Ride> rideResponse = restTemplate.postForEntity("/api/rides/" + driver.getUserId(),
                                ride, Ride.class);

                assertEquals(HttpStatus.OK, rideResponse.getStatusCode());
                assertNotNull(rideResponse.getBody());

                Ride createdRide = rideResponse.getBody();
                assertEquals("CityA", createdRide.getSource());
                assertEquals("CityB", createdRide.getDestination());
                assertEquals(3, createdRide.getTotalSeats());

                Long rideId = createdRide.getId();

                ResponseEntity<Booking> bookingResponse = restTemplate.postForEntity(
                                "/api/bookings?riderId=" + rider.getUserId()
                                                + "&rideId=" + rideId
                                                + "&seats=2",
                                null,
                                Booking.class);

                assertEquals(HttpStatus.OK, bookingResponse.getStatusCode());
                assertNotNull(bookingResponse.getBody());

                Booking booking = bookingResponse.getBody();
                assertEquals(2, booking.getSeatsBooked());
                assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        }
}
