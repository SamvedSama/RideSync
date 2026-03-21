package com.carpool.regression;

import com.carpool.TestUtils;
import com.carpool.dto.AuthResponse;
import com.carpool.dto.BookingResponse;
import com.carpool.dto.RegisterRequest;
import com.carpool.model.*;
import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegressionTestSuite — guards against regressions across all use cases.
 *
 * - Duplicate registration
 * - Overbooking prevention
 * - Double-booking same ride by driver (role guard)
 * - Duplicate payment prevention
 * - Refund of non-SUCCESS payment
 * - Booking on cancelled ride
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RegressionTestSuite {

    @Autowired
    private TestRestTemplate restTemplate;

    // ── Duplicate registration ────────────────────────────────────────────────

    @Test
    void duplicateRegistrationShouldFail() {
        RegisterRequest req = new RegisterRequest("Dup", "dup@reg.com", "password123", "9999", UserRole.RIDER);
        restTemplate.postForEntity("/api/users/register", req, AuthResponse.class);

        ResponseEntity<String> second = restTemplate.postForEntity("/api/users/register", req, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, second.getStatusCode());
        assertTrue(second.getBody().contains("already registered"));
    }

    // ── Overbooking ───────────────────────────────────────────────────────────

    @Test
    void overbookingShouldFail() {
        AuthResponse driver = TestUtils.register(restTemplate, "Driver", "driver@regress.com", "pass123", "111", UserRole.DRIVER);
        AuthResponse rider  = TestUtils.register(restTemplate, "Rider",  "rider@regress.com",  "pass123", "222", UserRole.RIDER);

        String rideJson = """
                {"source":"X","destination":"Y","totalSeats":1,"farePerSeat":100.0,"departureTime":"2030-12-01T09:00:00"}
                """;
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        // Book the only seat
        restTemplate.exchange("/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), BookingResponse.class);

        // Try to book again — no seats left
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── Driver cannot book own ride ───────────────────────────────────────────

    @Test
    void driverCannotBookOwnRide_returns403() {
        AuthResponse driver = TestUtils.register(restTemplate, "SelfBook", "self@regress.com", "pass123", "333", UserRole.DRIVER);

        String rideJson = """
                {"source":"A","destination":"B","totalSeats":3,"farePerSeat":50.0,"departureTime":"2030-12-01T10:00:00"}
                """;
        restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);

        // DRIVER role → @PreAuthorize("hasRole('RIDER')") → 403
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/bookings?rideId=1&seats=1",
                HttpMethod.POST, TestUtils.authEntity(driver.getToken()), String.class);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    // ── Booking on cancelled ride ─────────────────────────────────────────────

    @Test
    void bookingCancelledRideShouldFail() {
        AuthResponse driver = TestUtils.register(restTemplate, "CancelDriver", "cdriver@regress.com", "pass123", "444", UserRole.DRIVER);
        AuthResponse rider  = TestUtils.register(restTemplate, "CancelRider",  "crider@regress.com",  "pass123", "555", UserRole.RIDER);

        String rideJson = """
                {"source":"P","destination":"Q","totalSeats":2,"farePerSeat":80.0,"departureTime":"2030-12-01T11:00:00"}
                """;
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        // Driver cancels the ride
        restTemplate.exchange("/api/rides/" + rideId + "/status?status=CANCELLED",
                HttpMethod.PUT, TestUtils.authEntity(driver.getToken()), Ride.class);

        // Rider tries to book cancelled ride → 400
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── Duplicate payment ─────────────────────────────────────────────────────

    @Test
    void duplicatePaymentShouldFail() {
        AuthResponse driver = TestUtils.register(restTemplate, "PayDriver", "pdriver@regress.com", "pass123", "666", UserRole.DRIVER);
        AuthResponse rider  = TestUtils.register(restTemplate, "PayRider",  "prider@regress.com",  "pass123", "777", UserRole.RIDER);

        String rideJson = """
                {"source":"A","destination":"B","totalSeats":2,"farePerSeat":100.0,"departureTime":"2030-12-01T12:00:00"}
                """;
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        ResponseEntity<BookingResponse> bookResp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), BookingResponse.class);
        Long bookingId = bookResp.getBody().getId();

        PaymentRequest payReq = new PaymentRequest(bookingId, PaymentMethod.UPI, 100.0);

        // First payment
        restTemplate.exchange("/api/payments", HttpMethod.POST,
                TestUtils.authEntity(payReq, rider.getToken()), PaymentResponse.class);

        // Second payment on same booking
        ResponseEntity<String> second = restTemplate.exchange("/api/payments", HttpMethod.POST,
                TestUtils.authEntity(payReq, rider.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, second.getStatusCode());
    }

    // ── Refund non-existent payment ───────────────────────────────────────────

    @Test
    void refundNonExistentPaymentShouldFail() {
        AuthResponse admin = TestUtils.register(restTemplate, "RefAdmin", "refadmin@regress.com", "pass123", "888", UserRole.ADMIN);

        ResponseEntity<String> refundResp = restTemplate.exchange(
                "/api/payments/99999/refund", HttpMethod.PUT,
                TestUtils.authEntity(admin.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, refundResp.getStatusCode());
    }

    // ── Unauthenticated booking attempt ──────────────────────────────────────

    @Test
    void unauthenticatedBookingReturns401() {
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/bookings?rideId=1&seats=1", null, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }
}
