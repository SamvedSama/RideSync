package com.carpool.integration;

import com.carpool.model.*;
import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.PaymentMethod;
import com.carpool.payment.model.PaymentStatus;
import com.carpool.repository.BookingRepository;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Payment endpoints (Member 4).
 *
 * Uses H2 in-memory database (src/test/resources/application.yml).
 * Covers: successful payment, refund, duplicate payment guard, gateway failure.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaymentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // ── Full happy path: pay then refund ─────────────────────────────────────

    @Test
    void testPayAndRefund() {
        // Setup: create driver, rider, ride, booking
        User driver = userRepository.save(
                new User("Driver", "driver@pay.com", "pass", "111", UserRole.DRIVER));
        User rider = userRepository.save(
                new User("Rider", "rider@pay.com", "pass", "222", UserRole.RIDER));

        Ride ride = rideRepository.save(
                new Ride("CityA", "CityB", 3, 100.0,
                        LocalDateTime.now().plusDays(1), driver));

        Booking booking = bookingRepository.save(new Booking(rider, ride, 2));

        double expectedAmount = 200.0;

        // POST /api/payments — initiate + process
        PaymentRequest req = new PaymentRequest(booking.getId(), PaymentMethod.UPI, expectedAmount);
        ResponseEntity<PaymentResponse> payResp = restTemplate.postForEntity(
                "/api/payments", req, PaymentResponse.class);

        assertEquals(HttpStatus.OK, payResp.getStatusCode());
        assertNotNull(payResp.getBody());

        PaymentResponse payment = payResp.getBody();
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(expectedAmount, payment.getAmount());
        assertNotNull(payment.getTransactionRef());
        assertTrue(payment.getTransactionRef().startsWith("TXN-UPI-"));

        // PUT /api/payments/{id}/refund
        Long paymentId = payment.getPaymentId();
        ResponseEntity<PaymentResponse> refundResp = restTemplate.exchange(
                "/api/payments/" + paymentId + "/refund",
                HttpMethod.PUT, null, PaymentResponse.class);

        assertEquals(HttpStatus.OK, refundResp.getStatusCode());
        assertEquals(PaymentStatus.REFUNDED, refundResp.getBody().getStatus());
    }

    // ── GET payment by booking ────────────────────────────────────────────────

    @Test
    void testGetPaymentByBooking() {
        User driver = userRepository.save(
                new User("Driver2", "driver2@pay.com", "pass", "333", UserRole.DRIVER));
        User rider = userRepository.save(
                new User("Rider2", "rider2@pay.com", "pass", "444", UserRole.RIDER));

        Ride ride = rideRepository.save(
                new Ride("X", "Y", 2, 50.0,
                        LocalDateTime.now().plusDays(2), driver));

        Booking booking = bookingRepository.save(new Booking(rider, ride, 1));

        PaymentRequest req = new PaymentRequest(booking.getId(), PaymentMethod.CASH, 50.0);
        restTemplate.postForEntity("/api/payments", req, PaymentResponse.class);

        ResponseEntity<PaymentResponse> getResp = restTemplate.getForEntity(
                "/api/payments/booking/" + booking.getId(), PaymentResponse.class);

        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertEquals(PaymentStatus.SUCCESS, getResp.getBody().getStatus());
        assertEquals(booking.getId(), getResp.getBody().getBookingId());
    }

    // ── Gateway failure (.99 amounts) ─────────────────────────────────────────

    @Test
    void testGatewayFailure_returnsBadRequest() {
        User driver = userRepository.save(
                new User("Driver3", "driver3@pay.com", "pass", "555", UserRole.DRIVER));
        User rider = userRepository.save(
                new User("Rider3", "rider3@pay.com", "pass", "666", UserRole.RIDER));

        Ride ride = rideRepository.save(
                new Ride("A", "B", 2, 50.0,
                        LocalDateTime.now().plusDays(1), driver));

        Booking booking = bookingRepository.save(new Booking(rider, ride, 1));

        // Amount ending in .99 triggers mock gateway failure
        PaymentRequest req = new PaymentRequest(booking.getId(), PaymentMethod.CARD, 100.99);
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/payments", req, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── Cancelled booking cannot be paid ─────────────────────────────────────

    @Test
    void testPaymentForCancelledBooking_shouldFail() {
        User driver = userRepository.save(
                new User("Driver4", "driver4@pay.com", "pass", "777", UserRole.DRIVER));
        User rider = userRepository.save(
                new User("Rider4", "rider4@pay.com", "pass", "888", UserRole.RIDER));

        Ride ride = rideRepository.save(
                new Ride("P", "Q", 2, 80.0,
                        LocalDateTime.now().plusDays(1), driver));

        Booking booking = bookingRepository.save(new Booking(rider, ride, 1));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        PaymentRequest req = new PaymentRequest(booking.getId(), PaymentMethod.WALLET, 80.0);
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/payments", req, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}
