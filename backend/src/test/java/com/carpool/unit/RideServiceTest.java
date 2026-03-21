package com.carpool.unit;

import com.carpool.model.*;
import com.carpool.observer.*;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import com.carpool.service.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RideServiceTest {

    private RideRepository rideRepo;
    private UserRepository userRepo;
    private RideServiceImpl service;

    private User driver;

    @BeforeEach
    void setUp() {
        rideRepo = Mockito.mock(RideRepository.class);
        userRepo = Mockito.mock(UserRepository.class);
        service = new RideServiceImpl(rideRepo, userRepo);

        driver = new User("Driver", "driver@test.com", "hash", "111", UserRole.DRIVER);
        driver.setUserId(1L);

        when(rideRepo.save(any())).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            if (r.getId() == null)
                setId(r, 10L);
            return r;
        });
    }

    // ── createRide ────────────────────────────────────────────────────────────

    @Test
    void testCreateRide_success_setsPublishedStatus() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));

        Ride ride = new Ride("A", "B", 3, 100.0, LocalDateTime.now().plusDays(1), null);
        Ride result = service.createRide(1L, ride);

        assertEquals(RideStatus.PUBLISHED, result.getStatus());
        assertEquals(driver, result.getDriver());
        verify(rideRepo, times(1)).save(ride);
    }

    @Test
    void testCreateRide_nonDriverUser_throwsException() {
        User rider = new User("Rider", "rider@test.com", "hash", "222", UserRole.RIDER);
        rider.setUserId(2L);
        when(userRepo.findById(2L)).thenReturn(Optional.of(rider));

        Ride ride = new Ride("A", "B", 3, 100.0, LocalDateTime.now().plusDays(1), null);
        assertThrows(RuntimeException.class, () -> service.createRide(2L, ride));
        verify(rideRepo, never()).save(any());
    }

    @Test
    void testCreateRide_driverNotFound_throwsException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        Ride ride = new Ride("A", "B", 3, 100.0, LocalDateTime.now().plusDays(1), null);
        assertThrows(RuntimeException.class, () -> service.createRide(99L, ride));
    }

    @Test
    void testCreateRide_adminUser_throwsException() {
        User admin = new User("Admin", "admin@test.com", "hash", "000", UserRole.ADMIN);
        admin.setUserId(3L);
        when(userRepo.findById(3L)).thenReturn(Optional.of(admin));

        Ride ride = new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), null);
        assertThrows(RuntimeException.class, () -> service.createRide(3L, ride));
    }

    // ── getRide ───────────────────────────────────────────────────────────────

    @Test
    void testGetRide_exists_returnsRide() {
        Ride ride = new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), driver);
        setId(ride, 5L);
        when(rideRepo.findByIdWithBookings(5L)).thenReturn(Optional.of(ride));

        Ride result = service.getRide(5L);
        assertEquals("A", result.getSource());
    }

    @Test
    void testGetRide_notFound_throwsException() {
        when(rideRepo.findByIdWithBookings(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getRide(99L));
    }

    // ── updateRideStatus ──────────────────────────────────────────────────────

    @Test
    void testUpdateRideStatus_publishedToInProgress() {
        Ride ride = new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), driver);
        setId(ride, 10L);
        ride.setStatus(RideStatus.PUBLISHED);
        when(rideRepo.findByIdWithBookings(10L)).thenReturn(Optional.of(ride));

        Ride result = service.updateRideStatus(10L, RideStatus.IN_PROGRESS);
        assertEquals(RideStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void testUpdateRideStatus_toCompleted() {
        Ride ride = new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), driver);
        setId(ride, 11L);
        ride.setStatus(RideStatus.IN_PROGRESS);
        when(rideRepo.findByIdWithBookings(11L)).thenReturn(Optional.of(ride));

        Ride result = service.updateRideStatus(11L, RideStatus.COMPLETED);
        assertEquals(RideStatus.COMPLETED, result.getStatus());
    }

    @Test
    void testUpdateRideStatus_rideNotFound_throwsException() {
        when(rideRepo.findByIdWithBookings(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.updateRideStatus(99L, RideStatus.CANCELLED));
    }

    // ── getRidesByDriver ──────────────────────────────────────────────────────

    @Test
    void testGetRidesByDriver_returnsList() {
        Ride r1 = new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), driver);
        Ride r2 = new Ride("C", "D", 3, 80.0, LocalDateTime.now().plusDays(2), driver);
        when(rideRepo.findByDriverUserId(1L)).thenReturn(List.of(r1, r2));

        List<Ride> rides = service.getRidesByDriver(1L);
        assertEquals(2, rides.size());
    }

    @Test
    void testGetRidesByDriver_noRides_returnsEmptyList() {
        when(rideRepo.findByDriverUserId(1L)).thenReturn(List.of());
        List<Ride> rides = service.getRidesByDriver(1L);
        assertTrue(rides.isEmpty());
    }

    // ── getAllRides ───────────────────────────────────────────────────────────

    @Test
    void testGetAllRides_returnsList() {
        when(rideRepo.findAll()).thenReturn(List.of(
                new Ride("A", "B", 2, 50.0, LocalDateTime.now().plusDays(1), driver),
                new Ride("C", "D", 3, 80.0, LocalDateTime.now().plusDays(2), driver)));
        assertEquals(2, service.getAllRides().size());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static void setId(Ride r, Long id) {
        try {
            var f = Ride.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(r, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
