package com.carpool.system;

import com.carpool.TestUtils;
import com.carpool.dto.AuthResponse;
import com.carpool.dto.BookingResponse;
import com.carpool.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end system flow test.
 * Simulates a complete carpool journey: register → post ride → search → book → cancel → complete.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SystemFlowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullApplicationFlow() {

        // ── Step 1: Register a Driver ─────────────────────────────────────────
        AuthResponse driverAuth = TestUtils.register(restTemplate,
                "System Driver", "sysdriver@flow.com", "driverPass", "1111", UserRole.DRIVER);

        assertNotNull(driverAuth.getToken(), "Driver must receive a JWT token");
        assertEquals(UserRole.DRIVER, driverAuth.getRole());

        // ── Step 2: Register a Rider ──────────────────────────────────────────
        AuthResponse riderAuth = TestUtils.register(restTemplate,
                "System Rider", "sysrider@flow.com", "riderPass", "2222", UserRole.RIDER);

        assertNotNull(riderAuth.getToken(), "Rider must receive a JWT token");
        assertEquals(UserRole.RIDER, riderAuth.getRole());

        // ── Step 3: Driver posts a ride ───────────────────────────────────────
        String rideJson = """
                {
                  "source": "DowntownA",
                  "destination": "UpTownB",
                  "totalSeats": 3,
                  "farePerSeat": 150.0,
                  "departureTime": "2030-06-15T08:30:00"
                }
                """;
        ResponseEntity<Ride> rideResp = restTemplate.exchange(
                "/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driverAuth.getToken()), Ride.class);

        assertEquals(HttpStatus.OK, rideResp.getStatusCode());
        Ride ride = rideResp.getBody();
        assertNotNull(ride);
        assertEquals("DowntownA", ride.getSource());
        assertEquals("UpTownB", ride.getDestination());
        assertEquals(RideStatus.PUBLISHED, ride.getStatus());
        Long rideId = ride.getId();

        // ── Step 4: Rider searches for the ride ───────────────────────────────
        ResponseEntity<Ride[]> searchResp = restTemplate.getForEntity(
                "/api/rides/search?source=DowntownA&destination=UpTownB", Ride[].class);

        assertEquals(HttpStatus.OK, searchResp.getStatusCode());
        assertTrue(searchResp.getBody().length >= 1, "Search should return at least 1 ride");

        // ── Step 5: Rider books 2 seats ───────────────────────────────────────
        ResponseEntity<BookingResponse> bookResp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=2",
                HttpMethod.POST,
                TestUtils.authEntity(riderAuth.getToken()),
                BookingResponse.class);

        assertEquals(HttpStatus.OK, bookResp.getStatusCode());
        BookingResponse booking = bookResp.getBody();
        assertNotNull(booking);
        assertEquals(2, booking.getSeatsBooked());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(300.0, booking.getTotalFare(), 0.01);
        Long bookingId = booking.getId();

        // ── Step 6: Rider checks their bookings ───────────────────────────────
        ResponseEntity<BookingResponse[]> myBookingsResp = restTemplate.exchange(
                "/api/bookings/my-bookings", HttpMethod.GET,
                TestUtils.authEntity(riderAuth.getToken()), BookingResponse[].class);

        assertEquals(HttpStatus.OK, myBookingsResp.getStatusCode());
        assertEquals(1, myBookingsResp.getBody().length);

        // ── Step 7: Driver checks their rides ─────────────────────────────────
        ResponseEntity<Ride[]> myRidesResp = restTemplate.exchange(
                "/api/rides/my-rides", HttpMethod.GET,
                TestUtils.authEntity(driverAuth.getToken()), Ride[].class);

        assertEquals(HttpStatus.OK, myRidesResp.getStatusCode());
        assertEquals(1, myRidesResp.getBody().length);

        // ── Step 8: Driver sets ride to IN_PROGRESS ───────────────────────────
        ResponseEntity<Ride> inProgressResp = restTemplate.exchange(
                "/api/rides/" + rideId + "/status?status=IN_PROGRESS",
                HttpMethod.PUT,
                TestUtils.authEntity(driverAuth.getToken()), Ride.class);

        assertEquals(HttpStatus.OK, inProgressResp.getStatusCode());
        assertEquals(RideStatus.IN_PROGRESS, inProgressResp.getBody().getStatus());

        // ── Step 9: Rider cancels the booking ─────────────────────────────────
        ResponseEntity<BookingResponse> cancelResp = restTemplate.exchange(
                "/api/bookings/" + bookingId + "/cancel",
                HttpMethod.PUT,
                TestUtils.authEntity(riderAuth.getToken()), BookingResponse.class);

        assertEquals(HttpStatus.OK, cancelResp.getStatusCode());
        assertEquals(BookingStatus.CANCELLED, cancelResp.getBody().getStatus());

        // ── Step 10: Driver completes the ride ────────────────────────────────
        ResponseEntity<Ride> completedResp = restTemplate.exchange(
                "/api/rides/" + rideId + "/status?status=COMPLETED",
                HttpMethod.PUT,
                TestUtils.authEntity(driverAuth.getToken()), Ride.class);

        assertEquals(HttpStatus.OK, completedResp.getStatusCode());
        assertEquals(RideStatus.COMPLETED, completedResp.getBody().getStatus());

        // ── Step 11: Trying to book a completed ride fails ────────────────────
        ResponseEntity<String> lateBookResp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST,
                TestUtils.authEntity(riderAuth.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, lateBookResp.getStatusCode());
    }
}
