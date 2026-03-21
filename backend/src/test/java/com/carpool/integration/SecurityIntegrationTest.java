package com.carpool.integration;

import com.carpool.TestUtils;
import com.carpool.dto.AuthResponse;
import com.carpool.model.UserRole;
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
class SecurityIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        private String uid() {
                return UUID.randomUUID().toString().replace("-", "");
        }

        // ── Public endpoints must be accessible without auth ──────────────────────

        @Test
        void publicEndpoint_searchRides_noAuthRequired() {
                ResponseEntity<String> resp = restTemplate.getForEntity(
                                "/api/rides/search?source=A&destination=B", String.class);
                assertNotEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
                assertNotEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        @Test
        void publicEndpoint_getRideById_noAuthRequired() {
                ResponseEntity<String> resp = restTemplate.getForEntity("/api/rides/1", String.class);
                assertNotEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
                assertNotEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        @Test
        void publicEndpoint_register_noAuthRequired() {
                String u = uid();
                ResponseEntity<String> resp = restTemplate.postForEntity(
                                "/api/users/register",
                                new com.carpool.dto.RegisterRequest("T", u + "@t.com", "pass123", "1", UserRole.RIDER),
                                String.class);
                assertNotEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
                assertNotEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        // ── Protected endpoints reject unauthenticated requests ───────────────────

        @Test
        void protectedEndpoint_getMe_returns401WithoutToken() {
                assertEquals(HttpStatus.UNAUTHORIZED,
                                restTemplate.getForEntity("/api/users/me", String.class).getStatusCode());
        }

        @Test
        void protectedEndpoint_myRides_noTokenReturnsAuthError() {
                // @PreAuthorize("hasRole('DRIVER')") causes Spring to return 403 for anonymous
                // users
                // because the role check fires before the authenticationEntryPoint.
                // Both 401 and 403 correctly mean "not allowed without authentication".
                ResponseEntity<String> resp = restTemplate.getForEntity("/api/rides/my-rides", String.class);
                assertTrue(
                                resp.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                                                resp.getStatusCode() == HttpStatus.FORBIDDEN,
                                "Expected 401 or 403 but got: " + resp.getStatusCode());
        }

        @Test
        void protectedEndpoint_createRide_returns401WithoutToken() {
                HttpEntity<String> entity = new HttpEntity<>("{}",
                                new HttpHeaders() {
                                        {
                                                setContentType(MediaType.APPLICATION_JSON);
                                        }
                                });
                assertEquals(HttpStatus.UNAUTHORIZED,
                                restTemplate.exchange("/api/rides", HttpMethod.POST, entity, String.class)
                                                .getStatusCode());
        }

        @Test
        void protectedEndpoint_myBookings_returns401WithoutToken() {
                assertEquals(HttpStatus.UNAUTHORIZED,
                                restTemplate.getForEntity("/api/bookings/my-bookings", String.class).getStatusCode());
        }

        @Test
        void protectedEndpoint_adminUsers_returns401WithoutToken() {
                assertEquals(HttpStatus.UNAUTHORIZED,
                                restTemplate.getForEntity("/api/admin/users", String.class).getStatusCode());
        }

        @Test
        void protectedEndpoint_adminRides_returns401WithoutToken() {
                assertEquals(HttpStatus.UNAUTHORIZED,
                                restTemplate.getForEntity("/api/admin/rides", String.class).getStatusCode());
        }

        @Test
        void protectedEndpoint_adminAudit_returns401WithoutToken() {
                assertEquals(HttpStatus.UNAUTHORIZED,
                                restTemplate.getForEntity("/api/admin/audit", String.class).getStatusCode());
        }

        @Test
        void protectedEndpoint_payments_returns401WithoutToken() {
                assertEquals(HttpStatus.UNAUTHORIZED,
                                restTemplate.getForEntity("/api/payments", String.class).getStatusCode());
        }

        // ── Wrong role must get 403, not 401 ──────────────────────────────────────

        @Test
        void riderAccessingDriverEndpoint_returns403() {
                String u = uid();
                AuthResponse rider = TestUtils.register(restTemplate,
                                "SecRider", "secrider" + u + "@sec.com", "pass123", "111", UserRole.RIDER);

                String validRideJson = "{\"source\":\"A\",\"destination\":\"B\"," +
                                "\"totalSeats\":2,\"farePerSeat\":50.0," +
                                "\"departureTime\":\"2030-12-01T09:00:00\"}";
                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/rides", HttpMethod.POST,
                                TestUtils.authEntity(validRideJson, rider.getToken()), String.class);

                assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        @Test
        void riderAccessingAdminEndpoint_returns403() {
                String u = uid();
                AuthResponse rider = TestUtils.register(restTemplate,
                                "SecRider2", "secrider2" + u + "@sec.com", "pass123", "222", UserRole.RIDER);

                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/admin/users", HttpMethod.GET,
                                TestUtils.authEntity(rider.getToken()), String.class);

                assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        @Test
        void driverAccessingRiderBookingEndpoint_returns403() {
                String u = uid();
                AuthResponse driver = TestUtils.register(restTemplate,
                                "SecDriver", "secdriver" + u + "@sec.com", "pass123", "333", UserRole.DRIVER);

                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/bookings?rideId=1&seats=1", HttpMethod.POST,
                                TestUtils.authEntity(driver.getToken()), String.class);

                assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        @Test
        void driverAccessingMyBookings_returns403() {
                String u = uid();
                AuthResponse driver = TestUtils.register(restTemplate,
                                "SecDriver2", "secdriver2" + u + "@sec.com", "pass123", "444", UserRole.DRIVER);

                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/bookings/my-bookings", HttpMethod.GET,
                                TestUtils.authEntity(driver.getToken()), String.class);

                assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        // ── Tampered/invalid JWT token must get 401 ───────────────────────────────

        @Test
        void invalidToken_returns401() {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth("totally.invalid.token");
                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/users/me", HttpMethod.GET,
                                new HttpEntity<>(headers), String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        }

        @Test
        void malformedBearerHeader_returns401() {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "NotBearer abc123");
                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/users/me", HttpMethod.GET,
                                new HttpEntity<>(headers), String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        }
}
