package com.carpool.system;

import com.carpool.TestUtils;
import com.carpool.admin.model.AdminAuditLog;
import com.carpool.dto.AuthResponse;
import com.carpool.dto.BookingResponse;
import com.carpool.model.*;
import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.PaymentMethod;
import com.carpool.payment.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MultiUserSystemTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // Each test registers its own users with unique emails — no shared @BeforeEach state
    // that could be wiped between tests.

    private AuthResponse[] setupUsers(String uid) {
        AuthResponse driver = TestUtils.register(restTemplate, "Driver", "drv" + uid + "@m.com", "pass123", "1", UserRole.DRIVER);
        AuthResponse rider1 = TestUtils.register(restTemplate, "Rider1", "r1"  + uid + "@m.com", "pass123", "2", UserRole.RIDER);
        AuthResponse rider2 = TestUtils.register(restTemplate, "Rider2", "r2"  + uid + "@m.com", "pass123", "3", UserRole.RIDER);
        AuthResponse admin  = TestUtils.register(restTemplate, "Admin",  "adm" + uid + "@m.com", "pass123", "0", UserRole.ADMIN);
        return new AuthResponse[]{driver, rider1, rider2, admin};
    }

    // ── Two riders book the same ride ─────────────────────────────────────────

    @Test
    void twoRidersBookSameRide_bothSucceed_seatCountIsCorrect() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse[] users = setupUsers(uid);
        AuthResponse driver = users[0], rider1 = users[1], rider2 = users[2];

        String rideJson = "{\"source\":\"M\",\"destination\":\"N\",\"totalSeats\":4," +
                "\"farePerSeat\":100.0,\"departureTime\":\"2030-12-01T10:00:00\"}";
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        ResponseEntity<BookingResponse> b1 = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=2",
                HttpMethod.POST, TestUtils.authEntity(rider1.getToken()), BookingResponse.class);
        ResponseEntity<BookingResponse> b2 = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=2",
                HttpMethod.POST, TestUtils.authEntity(rider2.getToken()), BookingResponse.class);

        assertEquals(HttpStatus.OK, b1.getStatusCode());
        assertEquals(HttpStatus.OK, b2.getStatusCode());

        ResponseEntity<Ride> rideState = restTemplate.getForEntity("/api/rides/" + rideId, Ride.class);
        assertEquals(RideStatus.BOOKED, rideState.getBody().getStatus());
        assertEquals(0, rideState.getBody().getAvailableSeats());
    }

    @Test
    void secondRiderOverbooksAfterFirstFillsRide_returns400() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse[] users = setupUsers(uid);
        AuthResponse driver = users[0], rider1 = users[1], rider2 = users[2];

        String rideJson = "{\"source\":\"O\",\"destination\":\"P\",\"totalSeats\":2," +
                "\"farePerSeat\":80.0,\"departureTime\":\"2030-12-01T11:00:00\"}";
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        restTemplate.exchange("/api/bookings?rideId=" + rideId + "&seats=2",
                HttpMethod.POST, TestUtils.authEntity(rider1.getToken()), BookingResponse.class);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider2.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── Cancellation reopens seats ────────────────────────────────────────────

    @Test
    void riderCancels_rideReopens_anotherRiderCanBook() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse[] users = setupUsers(uid);
        AuthResponse driver = users[0], rider1 = users[1], rider2 = users[2];

        String rideJson = "{\"source\":\"Q\",\"destination\":\"R\",\"totalSeats\":1," +
                "\"farePerSeat\":60.0,\"departureTime\":\"2030-12-01T12:00:00\"}";
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        ResponseEntity<BookingResponse> b1 = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider1.getToken()), BookingResponse.class);
        Long bookingId = b1.getBody().getId();

        ResponseEntity<Ride> booked = restTemplate.getForEntity("/api/rides/" + rideId, Ride.class);
        assertEquals(RideStatus.BOOKED, booked.getBody().getStatus());

        restTemplate.exchange("/api/bookings/" + bookingId + "/cancel",
                HttpMethod.PUT, TestUtils.authEntity(rider1.getToken()), BookingResponse.class);

        ResponseEntity<Ride> reopened = restTemplate.getForEntity("/api/rides/" + rideId, Ride.class);
        assertEquals(RideStatus.PUBLISHED, reopened.getBody().getStatus());

        ResponseEntity<BookingResponse> b2 = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider2.getToken()), BookingResponse.class);

        assertEquals(HttpStatus.OK, b2.getStatusCode());
        assertEquals(BookingStatus.CONFIRMED, b2.getBody().getStatus());
    }

    // ── Full payment lifecycle ────────────────────────────────────────────────

    @Test
    void fullPaymentLifecycle_book_pay_refund() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse[] users = setupUsers(uid);
        AuthResponse driver = users[0], rider1 = users[1];

        String rideJson = "{\"source\":\"S\",\"destination\":\"T\",\"totalSeats\":2," +
                "\"farePerSeat\":150.0,\"departureTime\":\"2030-12-01T13:00:00\"}";
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        ResponseEntity<BookingResponse> bookResp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider1.getToken()), BookingResponse.class);
        Long bookingId = bookResp.getBody().getId();
        assertEquals(150.0, bookResp.getBody().getTotalFare(), 0.01);

        PaymentRequest payReq = new PaymentRequest(bookingId, PaymentMethod.UPI, 150.0);
        ResponseEntity<PaymentResponse> payResp = restTemplate.exchange(
                "/api/payments", HttpMethod.POST,
                TestUtils.authEntity(payReq, rider1.getToken()), PaymentResponse.class);
        assertEquals(HttpStatus.OK, payResp.getStatusCode());
        assertEquals(PaymentStatus.SUCCESS, payResp.getBody().getStatus());
        Long paymentId = payResp.getBody().getPaymentId();

        ResponseEntity<PaymentResponse> refundResp = restTemplate.exchange(
                "/api/payments/" + paymentId + "/refund",
                HttpMethod.PUT, TestUtils.authEntity(rider1.getToken()), PaymentResponse.class);
        assertEquals(HttpStatus.OK, refundResp.getStatusCode());
        assertEquals(PaymentStatus.REFUNDED, refundResp.getBody().getStatus());
    }

    // ── Admin bans a user mid-session ─────────────────────────────────────────

    @Test
    void adminBansRider_subsequentRequestsRejected() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String rider1Email = "r1" + uid + "@m.com";
        AuthResponse driver = TestUtils.register(restTemplate, "Driver", "drv" + uid + "@m.com", "pass123", "1", UserRole.DRIVER);
        AuthResponse rider1 = TestUtils.register(restTemplate, "Rider1", rider1Email,             "pass123", "2", UserRole.RIDER);
        AuthResponse admin  = TestUtils.register(restTemplate, "Admin",  "adm" + uid + "@m.com",  "pass123", "0", UserRole.ADMIN);

        restTemplate.exchange("/api/admin/users/" + rider1.getUserId() + "/ban",
                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

        ResponseEntity<String> loginResp = restTemplate.postForEntity(
                "/api/users/login",
                new com.carpool.dto.LoginRequest(rider1Email, "pass123"),
                String.class);
        assertEquals(HttpStatus.BAD_REQUEST, loginResp.getStatusCode());

        restTemplate.exchange("/api/admin/users/" + rider1.getUserId() + "/unban",
                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

        ResponseEntity<com.carpool.dto.AuthResponse> loginAgain = restTemplate.postForEntity(
                "/api/users/login",
                new com.carpool.dto.LoginRequest(rider1Email, "pass123"),
                com.carpool.dto.AuthResponse.class);
        assertEquals(HttpStatus.OK, loginAgain.getStatusCode());
        assertNotNull(loginAgain.getBody().getToken());
    }

    // ── Admin cancels a ride after booking ────────────────────────────────────

    @Test
    void adminCancelsRide_auditLogRecorded() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse[] users = setupUsers(uid);
        AuthResponse driver = users[0], admin = users[3];

        String rideJson = "{\"source\":\"U\",\"destination\":\"V\",\"totalSeats\":2," +
                "\"farePerSeat\":90.0,\"departureTime\":\"2030-12-01T14:00:00\"}";
        ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
        Long rideId = rideResp.getBody().getId();

        ResponseEntity<Ride> cancelResp = restTemplate.exchange(
                "/api/admin/rides/" + rideId + "/cancel",
                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), Ride.class);
        assertEquals(HttpStatus.OK, cancelResp.getStatusCode());
        assertEquals(RideStatus.CANCELLED, cancelResp.getBody().getStatus());

        ResponseEntity<AdminAuditLog[]> auditResp = restTemplate.exchange(
                "/api/admin/audit", HttpMethod.GET,
                TestUtils.authEntity(admin.getToken()), AdminAuditLog[].class);
        assertTrue(auditResp.getBody().length >= 1);

        boolean found = false;
        for (AdminAuditLog log : auditResp.getBody()) {
            if ("RIDE".equals(log.getTargetType()) && log.getTargetId().equals(rideId)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Audit log must contain the RIDE cancellation entry");
    }

    // ── Profile update is visible immediately ─────────────────────────────────

    @Test
    void profileUpdate_immediatelyReflectedOnGetMe() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse[] users = setupUsers(uid);
        AuthResponse rider1 = users[1];

        restTemplate.exchange(
                "/api/users/" + rider1.getUserId() + "?name=UpdatedRider&phone=9876543210",
                HttpMethod.PUT, TestUtils.authEntity(rider1.getToken()), User.class);

        ResponseEntity<User> meResp = restTemplate.exchange(
                "/api/users/me", HttpMethod.GET,
                TestUtils.authEntity(rider1.getToken()), User.class);

        assertEquals(HttpStatus.OK, meResp.getStatusCode());
        assertEquals("UpdatedRider", meResp.getBody().getName());
        assertEquals("9876543210", meResp.getBody().getPhone());
    }
}
