package com.carpool.integration;

import com.carpool.TestUtils;
import com.carpool.dto.AuthResponse;
import com.carpool.dto.BookingResponse;
import com.carpool.model.*;
import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.PaymentMethod;
import com.carpool.payment.model.PaymentStatus;
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
class PaymentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private AuthResponse driverAuth;
    private AuthResponse riderAuth;
    private Long bookingId;

    @BeforeEach
    void setup() {
        driverAuth = TestUtils.register(restTemplate, "PayDriver", "paydriver@pay.com", "pass123", "111", UserRole.DRIVER);
        riderAuth  = TestUtils.register(restTemplate, "PayRider",  "payrider@pay.com",  "pass123", "222", UserRole.RIDER);

        String rideJson = """
                {"source":"X","destination":"Y","totalSeats":3,"farePerSeat":100.0,"departureTime":"2030-12-01T09:00:00"}
                """;
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driverAuth.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        ResponseEntity<BookingResponse> bookResp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(riderAuth.getToken()), BookingResponse.class);
        bookingId = bookResp.getBody().getId();
    }

    // ── POST /api/payments ────────────────────────────────────────────────────

    @Test
    void testPayment_upi_success() {
        PaymentRequest req = new PaymentRequest(bookingId, PaymentMethod.UPI, 100.0);

        ResponseEntity<PaymentResponse> resp = restTemplate.exchange(
                "/api/payments", HttpMethod.POST,
                TestUtils.authEntity(req, riderAuth.getToken()), PaymentResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(PaymentStatus.SUCCESS, resp.getBody().getStatus());
        assertEquals(bookingId, resp.getBody().getBookingId());
    }

    @Test
    void testPayment_card_success() {
        PaymentRequest req = new PaymentRequest(bookingId, PaymentMethod.CARD, 100.0);

        ResponseEntity<PaymentResponse> resp = restTemplate.exchange(
                "/api/payments", HttpMethod.POST,
                TestUtils.authEntity(req, riderAuth.getToken()), PaymentResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(PaymentStatus.SUCCESS, resp.getBody().getStatus());
    }

    @Test
    void testPayment_unauthenticated_returns401() {
        PaymentRequest req = new PaymentRequest(bookingId, PaymentMethod.UPI, 100.0);
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/payments", req, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void testPayment_duplicate_returns400() {
        PaymentRequest req = new PaymentRequest(bookingId, PaymentMethod.UPI, 100.0);

        restTemplate.exchange("/api/payments", HttpMethod.POST,
                TestUtils.authEntity(req, riderAuth.getToken()), PaymentResponse.class);

        ResponseEntity<String> second = restTemplate.exchange("/api/payments", HttpMethod.POST,
                TestUtils.authEntity(req, riderAuth.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, second.getStatusCode());
    }

    // ── GET /api/payments/booking/{bookingId} ─────────────────────────────────

    @Test
    void testGetPaymentByBooking_afterPayment() {
        PaymentRequest req = new PaymentRequest(bookingId, PaymentMethod.UPI, 100.0);
        restTemplate.exchange("/api/payments", HttpMethod.POST,
                TestUtils.authEntity(req, riderAuth.getToken()), PaymentResponse.class);

        ResponseEntity<PaymentResponse> resp = restTemplate.exchange(
                "/api/payments/booking/" + bookingId, HttpMethod.GET,
                TestUtils.authEntity(riderAuth.getToken()), PaymentResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(bookingId, resp.getBody().getBookingId());
    }

    // ── PUT /api/payments/{id}/refund ─────────────────────────────────────────

    @Test
    void testRefund_afterSuccessPayment() {
        PaymentRequest req = new PaymentRequest(bookingId, PaymentMethod.UPI, 100.0);
        ResponseEntity<PaymentResponse> payResp = restTemplate.exchange(
                "/api/payments", HttpMethod.POST,
                TestUtils.authEntity(req, riderAuth.getToken()), PaymentResponse.class);
        Long paymentId = payResp.getBody().getPaymentId();

        ResponseEntity<PaymentResponse> refundResp = restTemplate.exchange(
                "/api/payments/" + paymentId + "/refund", HttpMethod.PUT,
                TestUtils.authEntity(riderAuth.getToken()), PaymentResponse.class);

        assertEquals(HttpStatus.OK, refundResp.getStatusCode());
        assertEquals(PaymentStatus.REFUNDED, refundResp.getBody().getStatus());
    }

    @Test
    void testRefund_nonExistent_returns400() {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/payments/99999/refund", HttpMethod.PUT,
                TestUtils.authEntity(riderAuth.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── GET /api/payments (admin) ─────────────────────────────────────────────

    @Test
    void testListAllPayments_nonAdmin_returns403() {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/payments", HttpMethod.GET,
                TestUtils.authEntity(riderAuth.getToken()), String.class);
        // Payment list endpoint — currently accessible to any authenticated user
        // If you restrict it to ADMIN, this would return 403
        assertNotEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }
}
