package com.carpool.unit;

import com.carpool.admin.model.AdminAction;
import com.carpool.admin.model.AdminAuditLog;
import com.carpool.admin.repository.AdminAuditLogRepository;
import com.carpool.admin.service.AdminServiceImpl;
import com.carpool.model.*;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private UserRepository userRepo;
    private RideRepository rideRepo;
    private AdminAuditLogRepository auditRepo;
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        userRepo     = Mockito.mock(UserRepository.class);
        rideRepo     = Mockito.mock(RideRepository.class);
        auditRepo    = Mockito.mock(AdminAuditLogRepository.class);
        adminService = new AdminServiceImpl(userRepo, rideRepo, auditRepo);

        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── banUser ───────────────────────────────────────────────────────────────

    @Test
    void testBanUser_success_setBannedTrue() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User rider = makeUser(2L, UserRole.RIDER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.of(rider));

        User result = adminService.banUser(1L, 2L);

        assertTrue(result.isBanned(), "Banned flag must be true");
        verify(auditRepo, times(1)).save(any(AdminAuditLog.class));
    }

    @Test
    void testBanUser_targetIsAdmin_throwsException() {
        User admin1 = makeUser(1L, UserRole.ADMIN);
        User admin2 = makeUser(2L, UserRole.ADMIN);
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin1));
        when(userRepo.findById(2L)).thenReturn(Optional.of(admin2));

        assertThrows(IllegalArgumentException.class, () -> adminService.banUser(1L, 2L));
        verify(auditRepo, never()).save(any());
    }

    @Test
    void testBanUser_userNotFound_throwsException() {
        User admin = makeUser(1L, UserRole.ADMIN);
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminService.banUser(1L, 99L));
    }

    // ── unbanUser ─────────────────────────────────────────────────────────────

    @Test
    void testUnbanUser_success_setBannedFalse() {
        User admin  = makeUser(1L, UserRole.ADMIN);
        User banned = makeUser(2L, UserRole.RIDER);
        banned.setBanned(true);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.of(banned));

        User result = adminService.unbanUser(1L, 2L);

        assertFalse(result.isBanned(), "Banned flag must be false after unban");
        verify(auditRepo, times(1)).save(any(AdminAuditLog.class));
    }

    // ── changeUserRole ────────────────────────────────────────────────────────

    @Test
    void testChangeUserRole_riderToDriver() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User rider = makeUser(2L, UserRole.RIDER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.of(rider));

        User result = adminService.changeUserRole(1L, 2L, "DRIVER");

        assertEquals(UserRole.DRIVER, result.getRole());
        verify(auditRepo, times(1)).save(any(AdminAuditLog.class));
    }

    @Test
    void testChangeUserRole_invalidRole_throwsException() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User rider = makeUser(2L, UserRole.RIDER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.of(rider));

        assertThrows(IllegalArgumentException.class,
                () -> adminService.changeUserRole(1L, 2L, "SUPERUSER"));
    }

    // ── adminCancelRide ───────────────────────────────────────────────────────

    @Test
    void testAdminCancelRide_success() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User driver = makeUser(3L, UserRole.DRIVER);
        Ride ride = new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), driver);
        setId(ride, 10L);
        ride.setStatus(RideStatus.PUBLISHED);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(rideRepo.findById(10L)).thenReturn(Optional.of(ride));

        Ride result = adminService.adminCancelRide(1L, 10L);

        assertEquals(RideStatus.CANCELLED, result.getStatus());
        verify(auditRepo, times(1)).save(any(AdminAuditLog.class));
    }

    @Test
    void testAdminCancelRide_alreadyCancelled_throwsException() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User driver = makeUser(3L, UserRole.DRIVER);
        Ride ride = new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), driver);
        setId(ride, 11L);
        ride.setStatus(RideStatus.CANCELLED);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(rideRepo.findById(11L)).thenReturn(Optional.of(ride));

        assertThrows(IllegalArgumentException.class, () -> adminService.adminCancelRide(1L, 11L));
    }

    @Test
    void testAdminCancelRide_completed_throwsException() {
        User admin = makeUser(1L, UserRole.ADMIN);
        User driver = makeUser(3L, UserRole.DRIVER);
        Ride ride = new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), driver);
        setId(ride, 12L);
        ride.setStatus(RideStatus.COMPLETED);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(rideRepo.findById(12L)).thenReturn(Optional.of(ride));

        assertThrows(IllegalArgumentException.class, () -> adminService.adminCancelRide(1L, 12L));
    }

    // ── getAllUsers / getAllRides ──────────────────────────────────────────────

    @Test
    void testGetAllUsers_returnsAll() {
        when(userRepo.findAll()).thenReturn(List.of(
                makeUser(1L, UserRole.ADMIN), makeUser(2L, UserRole.RIDER)));

        List<User> users = adminService.getAllUsers();
        assertEquals(2, users.size());
    }

    @Test
    void testGetAllRides_returnsAll() {
        User driver = makeUser(3L, UserRole.DRIVER);
        when(rideRepo.findAll()).thenReturn(List.of(
                new Ride("X", "Y", 2, 50, LocalDateTime.now().plusDays(1), driver)));

        List<Ride> rides = adminService.getAllRides();
        assertEquals(1, rides.size());
    }

    // ── assertAdmin (defence-in-depth at service layer) ───────────────────────

    @Test
    void testBanUser_nonAdminCaller_throwsException() {
        User rider = makeUser(1L, UserRole.RIDER);
        User target = makeUser(2L, UserRole.RIDER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(rider));
        when(userRepo.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(IllegalArgumentException.class, () -> adminService.banUser(1L, 2L));
        verify(auditRepo, never()).save(any());
    }

    @Test
    void testChangeUserRole_nonAdminCaller_throwsException() {
        User driver = makeUser(1L, UserRole.DRIVER);
        User target = makeUser(2L, UserRole.RIDER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(userRepo.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(IllegalArgumentException.class,
                () -> adminService.changeUserRole(1L, 2L, "ADMIN"));
    }

    @Test
    void testAdminCancelRide_nonAdminCaller_throwsException() {
        User driver = makeUser(1L, UserRole.DRIVER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));

        assertThrows(IllegalArgumentException.class, () -> adminService.adminCancelRide(1L, 99L));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User makeUser(Long id, UserRole role) {
        User u = new User("User" + id, "user" + id + "@test.com", "hash", "000", role);
        u.setUserId(id);
        return u;
    }

    private static void setId(Ride ride, Long id) {
        try {
            var f = Ride.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(ride, id);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
