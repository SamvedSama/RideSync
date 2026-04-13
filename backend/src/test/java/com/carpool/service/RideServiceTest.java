package com.carpool.service;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private RideServiceImpl rideService;

    private User testDriver;
    private Ride testRide;

    @BeforeEach
    void setUp() {
        testDriver = new User();
        testDriver.setUserId(1L);
        testDriver.setEmail("driver@test.com");
        testDriver.setRole(UserRole.DRIVER);

        testRide = new Ride();
        testRide.setId(1L);
        testRide.setSource("Source");
        testRide.setDestination("Destination");
        testRide.setTotalSeats(4);
        testRide.setFarePerSeat(100.0);
        testRide.setDepartureTime(LocalDateTime.now().plusHours(1));
        testRide.setStatus(RideStatus.PUBLISHED);
        testRide.setDriver(testDriver);
    }

    @Test
    void testCreateRide() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        Ride result = rideService.createRide(1L, testRide);

        assertNotNull(result);
        assertEquals("Source", result.getSource());
        assertEquals("Destination", result.getDestination());
        assertEquals(RideStatus.PUBLISHED, result.getStatus());
        verify(rideRepository).save(any(Ride.class));
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetRide() {
        when(rideRepository.findById(1L)).thenReturn(Optional.of(testRide));

        Ride result = rideService.getRide(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(rideRepository).findById(1L);
    }

    @Test
    void testGetRideNotFound() {
        when(rideRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            rideService.getRide(1L);
        });
    }

    @Test
    void testUpdateRideStatus() {
        testRide.setStatus(RideStatus.IN_PROGRESS);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(testRide));
        when(rideRepository.save(testRide)).thenReturn(testRide);

        Ride result = rideService.updateRideStatus(1L, RideStatus.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(RideStatus.IN_PROGRESS, result.getStatus());
        verify(rideRepository).save(testRide);
    }

    @Test
    void testUpdateRideStatusToCompleted() {
        testRide.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(testRide));
        when(rideRepository.save(testRide)).thenReturn(testRide);

        Ride result = rideService.updateRideStatus(1L, RideStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(RideStatus.COMPLETED, result.getStatus());
        verify(rideRepository).save(testRide);
    }

    @Test
    void testUpdateRideStatusToCancelled() {
        testRide.setStatus(RideStatus.CANCELLED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(testRide));
        when(rideRepository.save(testRide)).thenReturn(testRide);

        Ride result = rideService.updateRideStatus(1L, RideStatus.CANCELLED);

        assertNotNull(result);
        assertEquals(RideStatus.CANCELLED, result.getStatus());
        verify(rideRepository).save(testRide);
    }

    @Test
    void testGetRidesByDriver() {
        List<Ride> rides = Arrays.asList(testRide);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findByDriverIdOrderByDepartureTimeDesc(1L)).thenReturn(rides);

        List<Ride> result = rideService.getRidesByDriver(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDriver, result.get(0).getDriver());
        verify(userRepository).findById(1L);
        verify(rideRepository).findByDriverIdOrderByDepartureTimeDesc(1L);
    }

    @Test
    void testGetAllRides() {
        List<Ride> rides = Arrays.asList(testRide);
        when(rideRepository.findAll()).thenReturn(rides);

        List<Ride> result = rideService.getAllRides();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(rideRepository).findAll();
    }

    @Test
    void testGetAvailableRides() {
        testRide.setStatus(RideStatus.PUBLISHED);
        List<Ride> rides = Arrays.asList(testRide);
        when(rideRepository.findByStatuses(Arrays.asList(RideStatus.PUBLISHED))).thenReturn(rides);

        List<Ride> result = rideService.getAvailableRides();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RideStatus.PUBLISHED, result.get(0).getStatus());
        verify(rideRepository).findByStatuses(Arrays.asList(RideStatus.PUBLISHED));
    }

    @Test
    void testSearchRides() {
        List<Ride> rides = Arrays.asList(testRide);
        when(rideRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase("Source", "Destination")).thenReturn(rides);

        List<Ride> result = rideService.searchRides("Source", "Destination");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Source", result.get(0).getSource());
        assertEquals("Destination", result.get(0).getDestination());
        verify(rideRepository).findBySourceIgnoreCaseAndDestinationIgnoreCase("Source", "Destination");
    }

    @Test
    void testUpdateRideStatusNotFound() {
        when(rideRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            rideService.updateRideStatus(1L, RideStatus.IN_PROGRESS);
        });
        verify(rideRepository, never()).save(any());
    }

    @Test
    void testRideStatusTransitions() {
        // Test valid status transitions
        when(rideRepository.findById(1L)).thenReturn(Optional.of(testRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // PUBLISHED -> IN_PROGRESS
        Ride ongoingRide = rideService.updateRideStatus(1L, RideStatus.IN_PROGRESS);
        assertEquals(RideStatus.IN_PROGRESS, ongoingRide.getStatus());

        // IN_PROGRESS -> COMPLETED
        testRide.setStatus(RideStatus.IN_PROGRESS);
        Ride completedRide = rideService.updateRideStatus(1L, RideStatus.COMPLETED);
        assertEquals(RideStatus.COMPLETED, completedRide.getStatus());

        // Any status -> CANCELLED
        testRide.setStatus(RideStatus.PUBLISHED);
        Ride cancelledRide = rideService.updateRideStatus(1L, RideStatus.CANCELLED);
        assertEquals(RideStatus.CANCELLED, cancelledRide.getStatus());
    }
}
