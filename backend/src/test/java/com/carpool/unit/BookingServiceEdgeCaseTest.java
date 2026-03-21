package com.carpool.unit;

import com.carpool.model.*;
import com.carpool.repository.BookingRepository;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import com.carpool.service.impl.BookingServiceImpl;
import com.carpool.service.strategy.StandardFareStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Edge-case and boundary tests for BookingService.
 * Complements BookingServiceTest with additional scenarios.
 */
class BookingServiceEdgeCaseTest {

    private BookingRepository bookingRepo;
    private RideRepository rideRepo;
    private UserRepository userRepo;
    private BookingServiceImpl service;

    @BeforeEach
    void setUp() {
        bookingRepo = Mockito.mock(BookingRepository.class);
        rideRepo = Mockito.mock(RideRepository.class);
        userRepo = Mockito.mock(UserRepository.class);
        service = new BookingServiceImpl(bookingRepo, rideRepo, userRepo, new StandardFareStrategy());

        when(bookingRepo.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b != null && b.getId() == null)
                setId(b, 100L);
            return b;
        });
    }

    @Test
    void testBookRide_exactlyOneRemainingSeats_succeeds() {
        User driver = makeUser(1L, UserRole.DRIVER);
        User rider = makeUser(2L, UserRole.RIDER);
        Ride ride = makeRide(10L, driver, 1, RideStatus.PUBLISHED);

        when(userRepo.findById(2L)).thenReturn(Optional.of(rider));
        when(rideRepo.findById(10L)).thenReturn(Optional.of(ride));

        Booking result = service.bookRide(2L, 10L, 1);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        assertEquals(RideStatus.BOOKED, ride.getStatus()); // ride fully booked
    }

    @Test
    void testBookRide_negativeSeats_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 10L, -1));
    }

    @Test
    void testBookRide_rideInProgress_throwsException() {
        User driver = makeUser(1L, UserRole.DRIVER);
        User rider = makeUser(2L, UserRole.RIDER);
        Ride ride = makeRide(10L, driver, 3, RideStatus.IN_PROGRESS);

        when(userRepo.findById(2L)).thenReturn(Optional.of(rider));
        when(rideRepo.findById(10L)).thenReturn(Optional.of(ride));

        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 10L, 1));
    }

    @Test
    void testBookRide_rideCreatedStatus_throwsException() {
        User driver = makeUser(1L, UserRole.DRIVER);
        User rider = makeUser(2L, UserRole.RIDER);
        Ride ride = makeRide(10L, driver, 3, RideStatus.CREATED);

        when(userRepo.findById(2L)).thenReturn(Optional.of(rider));
        when(rideRepo.findById(10L)).thenReturn(Optional.of(ride));

        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 10L, 1));
    }

    @Test
    void testBookRide_userNotFound_throwsException() {
        User driver = makeUser(1L, UserRole.DRIVER);
        Ride ride = makeRide(10L, driver, 3, RideStatus.PUBLISHED);
        when(rideRepo.findById(10L)).thenReturn(Optional.of(ride));
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.bookRide(99L, 10L, 1));
    }

    @Test
    void testCancelBooking_notFound_throwsException() {
        when(bookingRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.cancelBooking(999L));
    }

    @Test
    void testCancelBooking_partialFill_rideRemainsBooked() {
        // Ride has 2 seats, 2 booked → status BOOKED. Cancel 1 → back to PUBLISHED
        User driver = makeUser(1L, UserRole.DRIVER);
        User rider = makeUser(2L, UserRole.RIDER);
        Ride ride = makeRide(20L, driver, 2, RideStatus.BOOKED);

        Booking booking = new Booking(rider, ride, 2);
        setId(booking, 60L);
        when(bookingRepo.findById(60L)).thenReturn(Optional.of(booking));
        when(bookingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.cancelBooking(60L);

        // After cancelling 2 seats in a 2-seat ride → available again → PUBLISHED
        assertEquals(RideStatus.PUBLISHED, ride.getStatus());
    }

    @Test
    void testGetBookingsByRider_returnsList() {
        User driver = makeUser(1L, UserRole.DRIVER);
        User rider = makeUser(2L, UserRole.RIDER);
        Ride ride = makeRide(10L, driver, 3, RideStatus.PUBLISHED);

        Booking b1 = new Booking(rider, ride, 1);
        Booking b2 = new Booking(rider, ride, 2);
        when(bookingRepo.findByRiderUserId(2L)).thenReturn(List.of(b1, b2));

        List<Booking> result = service.getBookingsByRider(2L);
        assertEquals(2, result.size());
    }

    @Test
    void testGetBookingsForRide_returnsList() {
        User driver = makeUser(1L, UserRole.DRIVER);
        User rider = makeUser(2L, UserRole.RIDER);
        Ride ride = makeRide(10L, driver, 3, RideStatus.PUBLISHED);

        Booking b = new Booking(rider, ride, 1);
        when(bookingRepo.findByRideId(10L)).thenReturn(List.of(b));

        List<Booking> result = service.getBookingsForRide(10L);
        assertEquals(1, result.size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User makeUser(Long id, UserRole role) {
        User u = new User("User" + id, "u" + id + "@test.com", "hash", "000", role);
        u.setUserId(id);
        return u;
    }

    private Ride makeRide(Long id, User driver, int seats, RideStatus status) {
        Ride r = new Ride("A", "B", seats, 100.0, LocalDateTime.now().plusDays(1), driver);
        setId(r, id);
        r.setStatus(status);
        return r;
    }

    private static void setId(Ride r, Long id) {
        try {
            var f = Ride.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(r, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setId(Booking b, Long id) {
        try {
            var f = Booking.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(b, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
