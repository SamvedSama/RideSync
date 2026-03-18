package com.carpool.unit;

import com.carpool.admin.model.AdminAuditLog;
import com.carpool.admin.repository.AdminAuditLogRepository;
import com.carpool.admin.service.AdminServiceImpl;
import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdminServiceImpl.
 *
 * Verifies: role enforcement, ban/unban, ride cancellation, audit logging.
 * Demonstrates DIP: all dependencies are mocked abstractions.
 */
class AdminServiceTest {

    private UserRepository userRepository;
    private RideRepository rideRepository;
    private AdminAuditLogRepository auditLogRepository;
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        rideRepository = Mockito.mock(RideRepository.class);
        auditLogRepository = Mockito.mock(AdminAuditLogRepository.class);
        adminService = new AdminServiceImpl(userRepository, rideRepository, auditLogRepository);

        // Default: save returns the argument
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── banUser ───────────────────────────────────────────────────────────────

    @Test
    void testBanUser_success() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User rider = makeUser(2L, UserRole.RIDER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(rider));

        User banned = adminService.banUser(1L, 2L);

        assertTrue(banned.getPhone().startsWith("BANNED:"));
    }

    @Test
    void testBanUser_nonAdminCaller_shouldFail() {
        User rider = makeUser(1L, UserRole.RIDER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(rider));

        assertThrows(IllegalArgumentException.class,
                () -> adminService.banUser(1L, 2L));
    }

    @Test
    void testBanUser_targetIsAdmin_shouldFail() {
        User admin1 = makeUser(1L, UserRole.ADMIN);
        User admin2 = makeUser(2L, UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin2));

        assertThrows(IllegalArgumentException.class,
                () -> adminService.banUser(1L, 2L));
    }

    // ── unbanUser ─────────────────────────────────────────────────────────────

    @Test
    void testUnbanUser_success() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User banned = makeUser(2L, UserRole.RIDER);
        banned.setPhone("BANNED:9999");
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(banned));

        User unbanned = adminService.unbanUser(1L, 2L);

        assertFalse(unbanned.getPhone().startsWith("BANNED:"));
        assertEquals("9999", unbanned.getPhone());
    }

    // ── changeUserRole ────────────────────────────────────────────────────────

    @Test
    void testChangeUserRole_riderToDriver() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User rider = makeUser(2L, UserRole.RIDER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(rider));

        User updated = adminService.changeUserRole(1L, 2L, "DRIVER");

        assertEquals(UserRole.DRIVER, updated.getRole());
    }

    @Test
    void testChangeUserRole_invalidRole_shouldFail() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User rider = makeUser(2L, UserRole.RIDER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(rider));

        assertThrows(IllegalArgumentException.class,
                () -> adminService.changeUserRole(1L, 2L, "SUPERUSER"));
    }

    // ── adminCancelRide ───────────────────────────────────────────────────────

    @Test
    void testAdminCancelRide_success() {
        User admin = makeUser(1L, UserRole.ADMIN);
        Ride ride = makeRide(10L, RideStatus.PUBLISHED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        Ride cancelled = adminService.adminCancelRide(1L, 10L);

        assertEquals(RideStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testAdminCancelRide_completedRide_shouldFail() {
        User admin = makeUser(1L, UserRole.ADMIN);
        Ride ride = makeRide(10L, RideStatus.COMPLETED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThrows(IllegalArgumentException.class,
                () -> adminService.adminCancelRide(1L, 10L));
    }

    @Test
    void testAdminCancelRide_alreadyCancelled_shouldFail() {
        User admin = makeUser(1L, UserRole.ADMIN);
        Ride ride = makeRide(10L, RideStatus.CANCELLED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThrows(IllegalArgumentException.class,
                () -> adminService.adminCancelRide(1L, 10L));
    }

    // ── getAuditLog ───────────────────────────────────────────────────────────

    @Test
    void testGetAuditLog_returnsAll() {
        when(auditLogRepository.findAll()).thenReturn(List.of(
                new AdminAuditLog(1L, com.carpool.admin.model.AdminAction.USER_BANNED, "USER", 2L, "test"),
                new AdminAuditLog(1L, com.carpool.admin.model.AdminAction.RIDE_CANCELLED, "RIDE", 5L, "test2")
        ));

        List<AdminAuditLog> logs = adminService.getAuditLog();

        assertEquals(2, logs.size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User makeUser(Long id, UserRole role) {
        User u = new User("Name" + id, "user" + id + "@test.com", "pass", "000", role);
        u.setUserId(id);
        return u;
    }

    private Ride makeRide(Long id, RideStatus status) {
        Ride r = new Ride();
        r.setId(id);
        r.setStatus(status);
        return r;
    }
}
