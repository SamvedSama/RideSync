package com.carpool.integration;

import com.carpool.TestUtils;
import com.carpool.dto.AuthResponse;
import com.carpool.dto.BookingResponse;
import com.carpool.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private AuthResponse driverAuth;
    private AuthResponse riderAuth;
    private Long rideId;

    @BeforeEach
    void setup() {
        // Register driver and rider
        driverAuth = TestUtils.register(restTemplate, "Driver", "driver@booking.com", "pass123", "111", UserRole.DRIVER);
        riderAuth  = TestUtils.register(restTemplate, "Rider",  "rider@booking.com",  "pass123", "222", UserRole.RIDER);

        // Driver creates a ride
        String rideJson = """
                {
                  "source": "CityA",
                  "destination": "CityB",
                  "totalSeats": 3,
                  "farePerSeat": 100.0,
                  "departureTime": "2030-12-01T09:00:00"
                }
                """;
        ResponseEntity<Ride> rideResp = restTemplate.exchange(
                "/api/rides",
                HttpMethod.POST,
                TestUtils.authEntity(rideJson, driverAuth.getToken()),
                Ride.class);
        assertEquals(HttpStatus.OK, rideResp.getStatusCode());
        rideId = rideResp.getBody().getId();
    }

    // ── Book ride ─────────────────────────────────────────────────────────────

    @Test
    void testBookRide_success() {
        ResponseEntity<BookingResponse> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=2",
                HttpMethod.POST,
                TestUtils.authEntity(riderAuth.getToken()),
                BookingResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());

        BookingResponse booking = resp.getBody();
        assertEquals(2, booking.getSeatsBooked());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(200.0, booking.getTotalFare(), 0.01);
        assertEquals(rideId, booking.getRideId());
    }

    @Test
    void testBookRide_unauthenticated_returns401() {
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/bookings?rideId=" + rideId + "&seats=1", null, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void testBookRide_driverCannotBook_returns403OrBadRequest() {
        // Driver tries to book their own ride
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST,
                TestUtils.authEntity(driverAuth.getToken()),
                String.class);

        // DRIVER role is rejected at the @PreAuthorize level → 403
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void testBookRide_overbooking_returns400() {
        // Book all 3 seats
        restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=3",
                HttpMethod.POST,
                TestUtils.authEntity(riderAuth.getToken()),
                BookingResponse.class);

        // Try to book 1 more — should fail
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST,
                TestUtils.authEntity(riderAuth.getToken()),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void testBookRide_zeroSeats_returns400() {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=0",
                HttpMethod.POST,
                TestUtils.authEntity(riderAuth.getToken()),
                String.class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── Cancel booking ────────────────────────────────────────────────────────

    @Test
    void testCancelBooking_success() {
        // First book
        ResponseEntity<BookingResponse> bookResp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST,
                TestUtils.authEntity(riderAuth.getToken()),
                BookingResponse.class);
        Long bookingId = bookResp.getBody().getId();

        // Then cancel
        ResponseEntity<BookingResponse> cancelResp = restTemplate.exchange(
                "/api/bookings/" + bookingId + "/cancel",
                HttpMethod.PUT,
                TestUtils.authEntity(riderAuth.getToken()),
                BookingResponse.class);

        assertEquals(HttpStatus.OK, cancelResp.getStatusCode());
        assertEquals(BookingStatus.CANCELLED, cancelResp.getBody().getStatus());
    }

    @Test
    void testCancelBooking_alreadyCancelled_returns400() {
        ResponseEntity<BookingResponse> bookResp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST,
                TestUtils.authEntity(riderAuth.getToken()),
                BookingResponse.class);
        Long bookingId = bookResp.getBody().getId();

        // Cancel once
        restTemplate.exchange("/api/bookings/" + bookingId + "/cancel",
                HttpMethod.PUT, TestUtils.authEntity(riderAuth.getToken()), BookingResponse.class);

        // Cancel again → 400
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/bookings/" + bookingId + "/cancel",
                HttpMethod.PUT, TestUtils.authEntity(riderAuth.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── Get my bookings ───────────────────────────────────────────────────────

    @Test
    void testGetMyBookings_returnsRiderBookings() {
        restTemplate.exchange("/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(riderAuth.getToken()), BookingResponse.class);

        ResponseEntity<BookingResponse[]> resp = restTemplate.exchange(
                "/api/bookings/my-bookings", HttpMethod.GET,
                TestUtils.authEntity(riderAuth.getToken()), BookingResponse[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().length >= 1);
        assertEquals(rideId, resp.getBody()[0].getRideId());
    }
}
