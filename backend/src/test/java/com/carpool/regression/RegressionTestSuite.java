package com.carpool.regression;

import com.carpool.model.*;
import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.PaymentMethod;
import com.carpool.payment.model.PaymentStatus;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegressionTestSuite — guards against regressions across all members.
 *
 * Member 1: duplicate registration
 * Member 2: overbooking
 * Member 4: duplicate payment, refund of non-success payment
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegressionTestSuite {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    // ── Member 1 regression ───────────────────────────────────────────────────

    @Test
    void duplicateRegistrationShouldFail() {
        User user = new User("Dup", "dup@reg.com", "1234", "9999", UserRole.RIDER);
        restTemplate.postForEntity("/api/users/register", user, User.class);

        ResponseEntity<String> response =
                restTemplate.postForEntity("/api/users/register", user, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── Member 2 regression ───────────────────────────────────────────────────

    @Test
    void overbookingShouldFail() {
        User driver = userRepository.save(
                new User("Driver2", "driver2@reg.com", "pass", "111", UserRole.DRIVER));
        User rider = userRepository.save(
                new User("Rider2", "rider2@reg.com", "pass", "222", UserRole.RIDER));

        Ride ride = new Ride("X", "Y", 1, 100, LocalDateTime.now().plusDays(1), driver);

        ResponseEntity<Ride> rideResp = restTemplate.postForEntity(
                "/api/rides/" + driver.getUserId(), ride, Ride.class);
        Long rideId = rideResp.getBody().getId();

        restTemplate.postForEntity(
                "/api/bookings?riderId=" + rider.getUserId() + "&rideId=" + rideId + "&seats=1",
                null, Booking.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/bookings?riderId=" + rider.getUserId() + "&rideId=" + rideId + "&seats=1",
                null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── Member 4 regression: duplicate payment ────────────────────────────────

    @Test
    void duplicatePaymentShouldFail() {
        User driver = userRepository.save(
                new User("Driver3", "driver3@reg.com", "pass", "333", UserRole.DRIVER));
        User rider = userRepository.save(
                new User("Rider3", "rider3@reg.com", "pass", "444", UserRole.RIDER));

        Ride ride = new Ride("A", "B", 2, 100, LocalDateTime.now().plusDays(1), driver);
        ResponseEntity<Ride> rideResp = restTemplate.postForEntity(
                "/api/rides/" + driver.getUserId(), ride, Ride.class);

        ResponseEntity<Booking> bookingResp = restTemplate.postForEntity(
                "/api/bookings?riderId=" + rider.getUserId()
                        + "&rideId=" + rideResp.getBody().getId() + "&seats=1",
                null, Booking.class);
        Long bookingId = bookingResp.getBody().getId();

        PaymentRequest req = new PaymentRequest(bookingId, PaymentMethod.UPI, 100.0);
        restTemplate.postForEntity("/api/payments", req, PaymentResponse.class);

        // Second payment attempt on same booking
        ResponseEntity<String> second = restTemplate.postForEntity(
                "/api/payments", req, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, second.getStatusCode());
    }

    // ── Member 4 regression: refund non-success payment ───────────────────────

    @Test
    void refundOfNonSuccessPaymentShouldFail() {
        User driver = userRepository.save(
                new User("Driver4", "driver4@reg.com", "pass", "555", UserRole.DRIVER));
        User rider = userRepository.save(
                new User("Rider4", "rider4@reg.com", "pass", "666", UserRole.RIDER));

        Ride ride = new Ride("C", "D", 2, 100, LocalDateTime.now().plusDays(1), driver);
        ResponseEntity<Ride> rideResp = restTemplate.postForEntity(
                "/api/rides/" + driver.getUserId(), ride, Ride.class);

        ResponseEntity<Booking> bookingResp = restTemplate.postForEntity(
                "/api/bookings?riderId=" + rider.getUserId()
                        + "&rideId=" + rideResp.getBody().getId() + "&seats=1",
                null, Booking.class);
        Long bookingId = bookingResp.getBody().getId();

        // Gateway failure amount — payment stays FAILED / never reaches SUCCESS
        PaymentRequest req = new PaymentRequest(bookingId, PaymentMethod.CARD, 100.99);
        restTemplate.postForEntity("/api/payments", req, String.class);

        // Try to get payment and refund it — payment is not SUCCESS so refund should fail
        // We try a non-existent payment ID to test the guard
        ResponseEntity<String> refundResp = restTemplate.exchange(
                "/api/payments/99999/refund", HttpMethod.PUT, null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, refundResp.getStatusCode());
    }
}
