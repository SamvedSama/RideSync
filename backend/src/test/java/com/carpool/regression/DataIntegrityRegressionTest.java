package com.carpool.regression;

import com.carpool.TestUtils;
import com.carpool.dto.AuthResponse;
import com.carpool.dto.BookingResponse;
import com.carpool.model.*;
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
class DataIntegrityRegressionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // Each test registers its own users with unique emails — no shared @BeforeEach
    // state that gets wiped between tests, which was causing token invalidation.

    private AuthResponse registerDriver(String uid) {
        return TestUtils.register(restTemplate, "Driver", "drv" + uid + "@data.com", "pass123", "1", UserRole.DRIVER);
    }

    private AuthResponse registerRider(String uid) {
        return TestUtils.register(restTemplate, "Rider", "rid" + uid + "@data.com", "pass123", "2", UserRole.RIDER);
    }

    private Long createRide(int seats, AuthResponse driver) {
        String json = String.format(
                "{\"source\":\"A\",\"destination\":\"B\",\"totalSeats\":%d," +
                "\"farePerSeat\":100.0,\"departureTime\":\"2030-12-01T09:00:00\"}", seats);
        return restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(json, driver.getToken()), Ride.class)
                .getBody().getId();
    }

    // ── Available seats decrease on booking ───────────────────────────────────

    @Test
    void availableSeats_decreaseAfterBooking() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse driver = registerDriver(uid);
        AuthResponse rider  = registerRider(uid);
        Long rideId = createRide(4, driver);

        ResponseEntity<BookingResponse> bookResp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=2",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), BookingResponse.class);
        assertEquals(HttpStatus.OK, bookResp.getStatusCode(), "Booking must succeed");

        // GET /api/rides/{id} now uses findByIdWithBookings (JOIN FETCH) so availableSeats is correct
        ResponseEntity<Ride> rideResp = restTemplate.getForEntity("/api/rides/" + rideId, Ride.class);
        assertEquals(HttpStatus.OK, rideResp.getStatusCode());
        assertEquals(2, rideResp.getBody().getAvailableSeats(),
                "Available seats must decrease by the number booked");
    }

    @Test
    void availableSeats_increaseAfterCancellation() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse driver = registerDriver(uid);
        AuthResponse rider  = registerRider(uid);
        Long rideId = createRide(3, driver);

        ResponseEntity<BookingResponse> book = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=3",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), BookingResponse.class);
        Long bookingId = book.getBody().getId();

        assertEquals(RideStatus.BOOKED,
                restTemplate.getForEntity("/api/rides/" + rideId, Ride.class).getBody().getStatus());

        restTemplate.exchange("/api/bookings/" + bookingId + "/cancel",
                HttpMethod.PUT, TestUtils.authEntity(rider.getToken()), BookingResponse.class);

        ResponseEntity<Ride> ride = restTemplate.getForEntity("/api/rides/" + rideId, Ride.class);
        assertEquals(3, ride.getBody().getAvailableSeats());
        assertEquals(RideStatus.PUBLISHED, ride.getBody().getStatus());
    }

    // ── Total fare computed correctly ─────────────────────────────────────────

    @Test
    void totalFare_isCorrectInBookingResponse() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse driver = registerDriver(uid);
        AuthResponse rider  = registerRider(uid);
        Long rideId = createRide(5, driver);

        ResponseEntity<BookingResponse> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=3",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), BookingResponse.class);

        assertEquals(300.0, resp.getBody().getTotalFare(), 0.01,
                "Total fare must be farePerSeat × seats = 100 × 3 = 300");
    }

    // ── Ride status transitions ───────────────────────────────────────────────

    @Test
    void rideStatusCanBeSetToCompleted() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse driver = registerDriver(uid);
        Long rideId = createRide(2, driver);

        restTemplate.exchange("/api/rides/" + rideId + "/status?status=IN_PROGRESS",
                HttpMethod.PUT, TestUtils.authEntity(driver.getToken()), Ride.class);

        ResponseEntity<Ride> resp = restTemplate.exchange(
                "/api/rides/" + rideId + "/status?status=COMPLETED",
                HttpMethod.PUT, TestUtils.authEntity(driver.getToken()), Ride.class);

        assertEquals(RideStatus.COMPLETED, resp.getBody().getStatus());
    }

    @Test
    void completedRide_cannotBeBooked() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse driver = registerDriver(uid);
        AuthResponse rider  = registerRider(uid);
        Long rideId = createRide(2, driver);

        restTemplate.exchange("/api/rides/" + rideId + "/status?status=IN_PROGRESS",
                HttpMethod.PUT, TestUtils.authEntity(driver.getToken()), Ride.class);
        restTemplate.exchange("/api/rides/" + rideId + "/status?status=COMPLETED",
                HttpMethod.PUT, TestUtils.authEntity(driver.getToken()), Ride.class);

        ResponseEntity<String> book = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, book.getStatusCode());
    }

    // ── Booking response contains correct ride fields ─────────────────────────

    @Test
    void bookingResponse_containsCorrectRideInfo() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse driver = registerDriver(uid);
        AuthResponse rider  = registerRider(uid);
        Long rideId = createRide(2, driver);

        ResponseEntity<BookingResponse> resp = restTemplate.exchange(
                "/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider.getToken()), BookingResponse.class);

        BookingResponse booking = resp.getBody();
        assertEquals(rideId, booking.getRideId());
        assertEquals("A", booking.getSource());
        assertEquals("B", booking.getDestination());
        assertEquals(100.0, booking.getFarePerSeat(), 0.01);
        assertEquals(1, booking.getSeatsBooked());
        assertEquals(rider.getUserId(), booking.getRiderId());
        assertEquals("Rider", booking.getRiderName());
    }

    // ── My rides only returns this driver's rides ─────────────────────────────

    @Test
    void myRides_onlyReturnsOwnRides() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse driver1 = TestUtils.register(restTemplate, "Driver1", "drv1" + uid + "@data.com", "pass123", "1", UserRole.DRIVER);
        AuthResponse driver2 = TestUtils.register(restTemplate, "Driver2", "drv2" + uid + "@data.com", "pass123", "3", UserRole.DRIVER);

        createRide(2, driver1);
        restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(
                        "{\"source\":\"C\",\"destination\":\"D\",\"totalSeats\":2,\"farePerSeat\":50.0,\"departureTime\":\"2030-12-01T10:00:00\"}",
                        driver2.getToken()), Ride.class);

        ResponseEntity<Ride[]> myRides = restTemplate.exchange(
                "/api/rides/my-rides", HttpMethod.GET,
                TestUtils.authEntity(driver1.getToken()), Ride[].class);

        assertEquals(1, myRides.getBody().length, "Driver must only see their own rides");
        assertEquals("A", myRides.getBody()[0].getSource());
    }

    // ── My bookings only returns this rider's bookings ────────────────────────

    @Test
    void myBookings_onlyReturnsOwnBookings() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        AuthResponse driver = registerDriver(uid);
        AuthResponse rider1 = TestUtils.register(restTemplate, "Rider1", "rid1" + uid + "@data.com", "pass123", "2", UserRole.RIDER);
        AuthResponse rider2 = TestUtils.register(restTemplate, "Rider2", "rid2" + uid + "@data.com", "pass123", "4", UserRole.RIDER);

        Long rideId = createRide(4, driver);

        restTemplate.exchange("/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider1.getToken()), BookingResponse.class);
        restTemplate.exchange("/api/bookings?rideId=" + rideId + "&seats=1",
                HttpMethod.POST, TestUtils.authEntity(rider2.getToken()), BookingResponse.class);

        ResponseEntity<BookingResponse[]> myBookings = restTemplate.exchange(
                "/api/bookings/my-bookings", HttpMethod.GET,
                TestUtils.authEntity(rider1.getToken()), BookingResponse[].class);

        assertEquals(1, myBookings.getBody().length, "Rider must only see their own bookings");
        assertEquals(rider1.getUserId(), myBookings.getBody()[0].getRiderId());
    }
}
