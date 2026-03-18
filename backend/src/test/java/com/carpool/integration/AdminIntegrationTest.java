package com.carpool.integration;

import com.carpool.admin.model.AdminAuditLog;
import com.carpool.model.*;
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
 * Integration tests for Admin Management endpoints (Member 4 minor use case).
 *
 * Covers: list users, ban/unban, role change, ride cancel, audit log.
 * All tests run against H2 in-memory DB via src/test/resources/application.yml.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    // ── GET /api/admin/users ──────────────────────────────────────────────────

    @Test
    void testGetAllUsers() {
        userRepository.save(new User("Alice", "alice@admin.com", "pass", "111", UserRole.RIDER));
        userRepository.save(new User("Bob", "bob@admin.com", "pass", "222", UserRole.DRIVER));

        ResponseEntity<User[]> resp = restTemplate.getForEntity("/api/admin/users", User[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().length >= 2);
    }

    // ── PUT /api/admin/users/{id}/ban ─────────────────────────────────────────

    @Test
    void testBanUser_success() {
        User admin = userRepository.save(
                new User("Admin", "admin@test.com", "pass", "000", UserRole.ADMIN));
        User rider = userRepository.save(
                new User("Rider", "rider@ban.com", "pass", "999", UserRole.RIDER));

        ResponseEntity<User> resp = restTemplate.exchange(
                "/api/admin/users/" + rider.getUserId() + "/ban?adminId=" + admin.getUserId(),
                HttpMethod.PUT, null, User.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().getPhone().startsWith("BANNED:"));
    }

    @Test
    void testBanUser_nonAdminCaller_shouldReturn400() {
        User rider1 = userRepository.save(
                new User("Rider1", "r1@ban.com", "pass", "111", UserRole.RIDER));
        User rider2 = userRepository.save(
                new User("Rider2", "r2@ban.com", "pass", "222", UserRole.RIDER));

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/admin/users/" + rider2.getUserId() + "/ban?adminId=" + rider1.getUserId(),
                HttpMethod.PUT, null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── PUT /api/admin/users/{id}/unban ───────────────────────────────────────

    @Test
    void testUnbanUser_success() {
        User admin = userRepository.save(
                new User("Admin2", "admin2@test.com", "pass", "000", UserRole.ADMIN));
        User banned = new User("Banned", "banned@test.com", "pass", "BANNED:777", UserRole.RIDER);
        banned = userRepository.save(banned);

        ResponseEntity<User> resp = restTemplate.exchange(
                "/api/admin/users/" + banned.getUserId() + "/unban?adminId=" + admin.getUserId(),
                HttpMethod.PUT, null, User.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertFalse(resp.getBody().getPhone().startsWith("BANNED:"));
    }

    // ── PUT /api/admin/users/{id}/role ────────────────────────────────────────

    @Test
    void testChangeUserRole_success() {
        User admin = userRepository.save(
                new User("Admin3", "admin3@test.com", "pass", "000", UserRole.ADMIN));
        User rider = userRepository.save(
                new User("NewDriver", "newdriver@test.com", "pass", "123", UserRole.RIDER));

        ResponseEntity<User> resp = restTemplate.exchange(
                "/api/admin/users/" + rider.getUserId() + "/role?adminId="
                        + admin.getUserId() + "&role=DRIVER",
                HttpMethod.PUT, null, User.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(UserRole.DRIVER, resp.getBody().getRole());
    }

    // ── GET /api/admin/rides ──────────────────────────────────────────────────

    @Test
    void testGetAllRides() {
        User driver = userRepository.save(
                new User("Driver", "driver@admin.com", "pass", "555", UserRole.DRIVER));
        rideRepository.save(
                new Ride("CityA", "CityB", 3, 100.0,
                        LocalDateTime.now().plusDays(1), driver));

        ResponseEntity<Ride[]> resp = restTemplate.getForEntity("/api/admin/rides", Ride[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().length >= 1);
    }

    // ── PUT /api/admin/rides/{id}/cancel ─────────────────────────────────────

    @Test
    void testAdminCancelRide_success() {
        User admin = userRepository.save(
                new User("Admin4", "admin4@test.com", "pass", "000", UserRole.ADMIN));
        User driver = userRepository.save(
                new User("Driver2", "driver2@admin.com", "pass", "666", UserRole.DRIVER));
        Ride ride = rideRepository.save(
                new Ride("X", "Y", 2, 50.0,
                        LocalDateTime.now().plusDays(3), driver));

        ResponseEntity<Ride> resp = restTemplate.exchange(
                "/api/admin/rides/" + ride.getId() + "/cancel?adminId=" + admin.getUserId(),
                HttpMethod.PUT, null, Ride.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(RideStatus.CANCELLED, resp.getBody().getStatus());
    }

    // ── GET /api/admin/audit ──────────────────────────────────────────────────

    @Test
    void testAuditLog_recordedAfterBan() {
        User admin = userRepository.save(
                new User("Admin5", "admin5@test.com", "pass", "000", UserRole.ADMIN));
        User rider = userRepository.save(
                new User("Target", "target@test.com", "pass", "321", UserRole.RIDER));

        restTemplate.exchange(
                "/api/admin/users/" + rider.getUserId() + "/ban?adminId=" + admin.getUserId(),
                HttpMethod.PUT, null, User.class);

        ResponseEntity<AdminAuditLog[]> auditResp = restTemplate.getForEntity(
                "/api/admin/audit", AdminAuditLog[].class);

        assertEquals(HttpStatus.OK, auditResp.getStatusCode());
        assertTrue(auditResp.getBody().length >= 1);
        assertEquals("USER", auditResp.getBody()[0].getTargetType());
    }

    // ── GET /api/admin/audit/by-admin ─────────────────────────────────────────

    @Test
    void testAuditLog_filterByAdmin() {
        User admin = userRepository.save(
                new User("Admin6", "admin6@test.com", "pass", "000", UserRole.ADMIN));
        User rider = userRepository.save(
                new User("Rider6", "rider6@test.com", "pass", "456", UserRole.RIDER));

        restTemplate.exchange(
                "/api/admin/users/" + rider.getUserId() + "/ban?adminId=" + admin.getUserId(),
                HttpMethod.PUT, null, User.class);

        ResponseEntity<AdminAuditLog[]> auditResp = restTemplate.getForEntity(
                "/api/admin/audit/by-admin?adminId=" + admin.getUserId(),
                AdminAuditLog[].class);

        assertEquals(HttpStatus.OK, auditResp.getStatusCode());
        assertTrue(auditResp.getBody().length >= 1);
        assertEquals(admin.getUserId(), auditResp.getBody()[0].getAdminId());
    }
}
