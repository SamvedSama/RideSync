package com.carpool.integration;

import com.carpool.TestUtils;
import com.carpool.admin.model.AdminAuditLog;
import com.carpool.dto.AuthResponse;
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
class AdminIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        // Every test creates its own users with UUID-unique emails.
        // AFTER_CLASS means the Spring context (and DB) is shared across all tests
        // in this class — users accumulate but never collide because of unique emails.
        // Tokens remain valid for the full lifetime of the shared context.

        private String uid() {
                return UUID.randomUUID().toString().replace("-", "");
        }

        // ── Role enforcement ──────────────────────────────────────────────────────

        @Test
        void testAdminEndpoints_unauthenticated_returns401() {
                ResponseEntity<String> resp = restTemplate.getForEntity("/api/admin/users", String.class);
                assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        }

        @Test
        void testAdminEndpoints_nonAdmin_returns403() {
                String u = uid();
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1",
                                UserRole.RIDER);

                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/admin/users", HttpMethod.GET,
                                TestUtils.authEntity(rider.getToken()), String.class);
                assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        // ── GET /api/admin/users ──────────────────────────────────────────────────

        @Test
        void testGetAllUsers_admin_succeeds() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1", UserRole.RIDER);
                TestUtils.register(restTemplate, "Driver", "drv" + u + "@t.com", "pass123", "2", UserRole.DRIVER);

                ResponseEntity<User[]> resp = restTemplate.exchange(
                                "/api/admin/users", HttpMethod.GET,
                                TestUtils.authEntity(admin.getToken()), User[].class);

                assertEquals(HttpStatus.OK, resp.getStatusCode());
                assertTrue(resp.getBody().length >= 3);
        }

        // ── Ban / Unban ───────────────────────────────────────────────────────────

        @Test
        void testBanUser_admin_success() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1",
                                UserRole.RIDER);

                ResponseEntity<User> resp = restTemplate.exchange(
                                "/api/admin/users/" + rider.getUserId() + "/ban",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

                assertEquals(HttpStatus.OK, resp.getStatusCode());
                assertTrue(resp.getBody().isBanned());
        }

        @Test
        void testBanUser_nonAdmin_returns403() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1",
                                UserRole.RIDER);

                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/admin/users/" + admin.getUserId() + "/ban",
                                HttpMethod.PUT, TestUtils.authEntity(rider.getToken()), String.class);

                assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        }

        @Test
        void testBanUser_targetIsAdmin_returns400() {
                String u = uid();
                AuthResponse admin1 = TestUtils.register(restTemplate, "Admin1", "adm1" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse admin2 = TestUtils.register(restTemplate, "Admin2", "adm2" + u + "@t.com", "pass123", "9",
                                UserRole.ADMIN);

                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/admin/users/" + admin2.getUserId() + "/ban",
                                HttpMethod.PUT, TestUtils.authEntity(admin1.getToken()), String.class);

                assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        }

        @Test
        void testUnbanUser_success() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1",
                                UserRole.RIDER);

                restTemplate.exchange("/api/admin/users/" + rider.getUserId() + "/ban",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

                ResponseEntity<User> resp = restTemplate.exchange(
                                "/api/admin/users/" + rider.getUserId() + "/unban",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

                assertEquals(HttpStatus.OK, resp.getStatusCode());
                assertFalse(resp.getBody().isBanned());
        }

        @Test
        void testBannedUser_cannotLogin() {
                String u = uid();
                String riderEmail = "rid" + u + "@t.com";
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", riderEmail, "pass123", "1",
                                UserRole.RIDER);

                restTemplate.exchange("/api/admin/users/" + rider.getUserId() + "/ban",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

                ResponseEntity<String> loginResp = restTemplate.postForEntity(
                                "/api/users/login",
                                new com.carpool.dto.LoginRequest(riderEmail, "pass123"),
                                String.class);

                assertEquals(HttpStatus.BAD_REQUEST, loginResp.getStatusCode());
                assertTrue(loginResp.getBody().contains("banned"));
        }

        // ── Change role ───────────────────────────────────────────────────────────

        @Test
        void testChangeUserRole_riderToDriver() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1",
                                UserRole.RIDER);

                ResponseEntity<User> resp = restTemplate.exchange(
                                "/api/admin/users/" + rider.getUserId() + "/role?role=DRIVER",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

                assertEquals(HttpStatus.OK, resp.getStatusCode());
                assertEquals(UserRole.DRIVER, resp.getBody().getRole());
        }

        @Test
        void testChangeUserRole_invalidRole_returns400() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1",
                                UserRole.RIDER);

                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/admin/users/" + rider.getUserId() + "/role?role=SUPERUSER",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), String.class);

                assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        }

        // ── Rides ─────────────────────────────────────────────────────────────────

        @Test
        void testGetAllRides_admin_succeeds() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse driver = TestUtils.register(restTemplate, "Driver", "drv" + u + "@t.com", "pass123", "2",
                                UserRole.DRIVER);

                String rideJson = "{\"source\":\"A\",\"destination\":\"B\",\"totalSeats\":2,\"farePerSeat\":50.0,\"departureTime\":\"2030-12-01T09:00:00\"}";
                restTemplate.exchange("/api/rides", HttpMethod.POST,
                                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);

                ResponseEntity<Ride[]> resp = restTemplate.exchange(
                                "/api/admin/rides", HttpMethod.GET,
                                TestUtils.authEntity(admin.getToken()), Ride[].class);

                assertEquals(HttpStatus.OK, resp.getStatusCode());
                assertTrue(resp.getBody().length >= 1);
        }

        @Test
        void testAdminCancelRide_success() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse driver = TestUtils.register(restTemplate, "Driver", "drv" + u + "@t.com", "pass123", "2",
                                UserRole.DRIVER);

                String rideJson = "{\"source\":\"X\",\"destination\":\"Y\",\"totalSeats\":3,\"farePerSeat\":75.0,\"departureTime\":\"2030-12-01T10:00:00\"}";
                ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
                Long rideId = rideResp.getBody().getId();

                ResponseEntity<Ride> cancelResp = restTemplate.exchange(
                                "/api/admin/rides/" + rideId + "/cancel",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), Ride.class);

                assertEquals(HttpStatus.OK, cancelResp.getStatusCode());
                assertEquals(RideStatus.CANCELLED, cancelResp.getBody().getStatus());
        }

        @Test
        void testAdminCancelRide_alreadyCancelled_returns400() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse driver = TestUtils.register(restTemplate, "Driver", "drv" + u + "@t.com", "pass123", "2",
                                UserRole.DRIVER);

                String rideJson = "{\"source\":\"P\",\"destination\":\"Q\",\"totalSeats\":2,\"farePerSeat\":60.0,\"departureTime\":\"2030-12-01T11:00:00\"}";
                ResponseEntity<Ride> rideResp = restTemplate.exchange("/api/rides", HttpMethod.POST,
                                TestUtils.authEntity(rideJson, driver.getToken()), Ride.class);
                Long rideId = rideResp.getBody().getId();

                restTemplate.exchange("/api/admin/rides/" + rideId + "/cancel",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), Ride.class);

                ResponseEntity<String> resp = restTemplate.exchange(
                                "/api/admin/rides/" + rideId + "/cancel",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), String.class);

                assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        }

        // ── Audit log ─────────────────────────────────────────────────────────────

        @Test
        void testAuditLog_recordedAfterBan() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1",
                                UserRole.RIDER);

                restTemplate.exchange("/api/admin/users/" + rider.getUserId() + "/ban",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

                ResponseEntity<AdminAuditLog[]> auditResp = restTemplate.exchange(
                                "/api/admin/audit", HttpMethod.GET,
                                TestUtils.authEntity(admin.getToken()), AdminAuditLog[].class);

                assertEquals(HttpStatus.OK, auditResp.getStatusCode());
                assertTrue(auditResp.getBody().length >= 1);

                boolean found = false;
                for (AdminAuditLog log : auditResp.getBody()) {
                        if ("USER".equals(log.getTargetType()) && log.getAdminId().equals(admin.getUserId())) {
                                found = true;
                                break;
                        }
                }
                assertTrue(found);
        }

        @Test
        void testAuditLog_filterByAdmin() {
                String u = uid();
                AuthResponse admin = TestUtils.register(restTemplate, "Admin", "adm" + u + "@t.com", "pass123", "0",
                                UserRole.ADMIN);
                AuthResponse rider = TestUtils.register(restTemplate, "Rider", "rid" + u + "@t.com", "pass123", "1",
                                UserRole.RIDER);

                restTemplate.exchange("/api/admin/users/" + rider.getUserId() + "/ban",
                                HttpMethod.PUT, TestUtils.authEntity(admin.getToken()), User.class);

                ResponseEntity<AdminAuditLog[]> auditResp = restTemplate.exchange(
                                "/api/admin/audit/by-admin", HttpMethod.GET,
                                TestUtils.authEntity(admin.getToken()), AdminAuditLog[].class);

                assertEquals(HttpStatus.OK, auditResp.getStatusCode());
                assertTrue(auditResp.getBody().length >= 1);
                for (AdminAuditLog log : auditResp.getBody()) {
                        assertEquals(admin.getUserId(), log.getAdminId());
                }
        }
}
