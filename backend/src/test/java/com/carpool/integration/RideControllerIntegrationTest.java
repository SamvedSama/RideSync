package com.carpool.integration;

import com.carpool.TestUtils;
import com.carpool.dto.AuthResponse;
import com.carpool.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full integration tests for RideController.
 * Covers create, search, get, status update, my-rides, and security boundaries.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RideControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private AuthResponse driverAuth;
    private AuthResponse riderAuth;
    private AuthResponse adminAuth;

    private static final String RIDE_JSON =
            "{\"source\":\"CityA\",\"destination\":\"CityB\"," +
            "\"totalSeats\":3,\"farePerSeat\":120.0," +
            "\"departureTime\":\"2030-12-01T09:00:00\"}";

    @BeforeEach
    void setup() {
        driverAuth = TestUtils.register(restTemplate, "Driver", "driver@ride.com", "pass123", "111", UserRole.DRIVER);
        riderAuth  = TestUtils.register(restTemplate, "Rider",  "rider@ride.com",  "pass123", "222", UserRole.RIDER);
        adminAuth  = TestUtils.register(restTemplate, "Admin",  "admin@ride.com",  "pass123", "000", UserRole.ADMIN);
    }

    // ── POST /api/rides ───────────────────────────────────────────────────────

    @Test
    void testCreateRide_driver_succeeds() {
        ResponseEntity<Ride> resp = restTemplate.exchange(
                "/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Ride ride = resp.getBody();
        assertNotNull(ride.getId());
        assertEquals("CityA", ride.getSource());
        assertEquals("CityB", ride.getDestination());
        assertEquals(RideStatus.PUBLISHED, ride.getStatus());
        assertEquals(3, ride.getTotalSeats());
        assertEquals(120.0, ride.getFarePerSeat(), 0.01);
    }

    @Test
    void testCreateRide_rider_returns403() {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, riderAuth.getToken()), String.class);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void testCreateRide_unauthenticated_returns401() {
        HttpEntity<String> entity = new HttpEntity<>(RIDE_JSON,
                new HttpHeaders() {{ setContentType(MediaType.APPLICATION_JSON); }});
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/rides", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void testCreateRide_missingSource_returns400() {
        String bad = "{\"source\":\"\",\"destination\":\"CityB\",\"totalSeats\":2,\"farePerSeat\":50.0,\"departureTime\":\"2030-12-01T09:00:00\"}";
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/rides", HttpMethod.POST,
                TestUtils.authEntity(bad, driverAuth.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void testCreateRide_zeroSeats_returns400() {
        String bad = "{\"source\":\"A\",\"destination\":\"B\",\"totalSeats\":0,\"farePerSeat\":50.0,\"departureTime\":\"2030-12-01T09:00:00\"}";
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/rides", HttpMethod.POST,
                TestUtils.authEntity(bad, driverAuth.getToken()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── GET /api/rides/search ─────────────────────────────────────────────────

    @Test
    void testSearchRides_matchingSourceAndDest_returnsRide() {
        restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);

        ResponseEntity<Ride[]> resp = restTemplate.getForEntity(
                "/api/rides/search?source=CityA&destination=CityB", Ride[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().length);
        assertEquals("CityA", resp.getBody()[0].getSource());
    }

    @Test
    void testSearchRides_caseInsensitive_returnsRide() {
        restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);

        ResponseEntity<Ride[]> resp = restTemplate.getForEntity(
                "/api/rides/search?source=citya&destination=cityb", Ride[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().length);
    }

    @Test
    void testSearchRides_noMatch_returnsEmptyArray() {
        ResponseEntity<Ride[]> resp = restTemplate.getForEntity(
                "/api/rides/search?source=Unknown&destination=Nowhere", Ride[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(0, resp.getBody().length);
    }

    @Test
    void testSearchRides_publicEndpoint_noAuthRequired() {
        ResponseEntity<Ride[]> resp = restTemplate.getForEntity(
                "/api/rides/search?source=X&destination=Y", Ride[].class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void testSearchRides_cancelledRide_notReturned() {
        ResponseEntity<Ride> created = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);
        Long rideId = created.getBody().getId();

        restTemplate.exchange("/api/rides/" + rideId + "/status?status=CANCELLED",
                HttpMethod.PUT, TestUtils.authEntity(driverAuth.getToken()), Ride.class);

        ResponseEntity<Ride[]> resp = restTemplate.getForEntity(
                "/api/rides/search?source=CityA&destination=CityB", Ride[].class);

        assertEquals(0, resp.getBody().length, "Cancelled rides must not appear in search");
    }

    // ── GET /api/rides/{id} ───────────────────────────────────────────────────

    @Test
    void testGetRideById_exists_returnsRide() {
        ResponseEntity<Ride> created = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);
        Long rideId = created.getBody().getId();

        ResponseEntity<Ride> resp = restTemplate.getForEntity("/api/rides/" + rideId, Ride.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(rideId, resp.getBody().getId());
    }

    @Test
    void testGetRideById_notFound_returns500OrBadRequest() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/rides/99999", String.class);
        assertTrue(resp.getStatusCode().isError());
    }

    // ── GET /api/rides/my-rides ───────────────────────────────────────────────

    @Test
    void testGetMyRides_driver_returnsOwnRides() {
        restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);
        restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);

        ResponseEntity<Ride[]> resp = restTemplate.exchange(
                "/api/rides/my-rides", HttpMethod.GET,
                TestUtils.authEntity(driverAuth.getToken()), Ride[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().length);
    }

    @Test
    void testGetMyRides_rider_returns403() {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/rides/my-rides", HttpMethod.GET,
                TestUtils.authEntity(riderAuth.getToken()), String.class);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    // ── PUT /api/rides/{id}/status ────────────────────────────────────────────

    @Test
    void testUpdateRideStatus_driver_succeeds() {
        ResponseEntity<Ride> created = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);
        Long rideId = created.getBody().getId();

        ResponseEntity<Ride> resp = restTemplate.exchange(
                "/api/rides/" + rideId + "/status?status=IN_PROGRESS",
                HttpMethod.PUT, TestUtils.authEntity(driverAuth.getToken()), Ride.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(RideStatus.IN_PROGRESS, resp.getBody().getStatus());
    }

    @Test
    void testUpdateRideStatus_admin_succeeds() {
        ResponseEntity<Ride> created = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);
        Long rideId = created.getBody().getId();

        ResponseEntity<Ride> resp = restTemplate.exchange(
                "/api/rides/" + rideId + "/status?status=CANCELLED",
                HttpMethod.PUT, TestUtils.authEntity(adminAuth.getToken()), Ride.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(RideStatus.CANCELLED, resp.getBody().getStatus());
    }

    @Test
    void testUpdateRideStatus_rider_returns403() {
        ResponseEntity<Ride> created = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);
        Long rideId = created.getBody().getId();

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/rides/" + rideId + "/status?status=CANCELLED",
                HttpMethod.PUT, TestUtils.authEntity(riderAuth.getToken()), String.class);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    // ── availableSeats in response ────────────────────────────────────────────

    @Test
    void testCreateRide_responseIncludesAvailableSeats() {
        ResponseEntity<Ride> resp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                TestUtils.authEntity(RIDE_JSON, driverAuth.getToken()), Ride.class);

        assertEquals(3, resp.getBody().getAvailableSeats(),
                "availableSeats should equal totalSeats when no bookings exist");
    }
}
