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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepo;
    private RideRepository rideRepo;
    private UserRepository userRepo;
    private BookingServiceImpl service;

    private User driver;
    private User rider;
    private Ride ride;

    @BeforeEach
    void setUp() {
        bookingRepo = Mockito.mock(BookingRepository.class);
        rideRepo = Mockito.mock(RideRepository.class);
        userRepo = Mockito.mock(UserRepository.class);
        service = new BookingServiceImpl(bookingRepo, rideRepo, userRepo, new StandardFareStrategy());

        driver = makeUser(1L, UserRole.DRIVER);
        rider = makeUser(2L, UserRole.RIDER);
        ride = makeRide(10L, driver, 3, RideStatus.PUBLISHED);

        when(userRepo.findById(2L)).thenReturn(Optional.of(rider));
        when(rideRepo.findById(10L)).thenReturn(Optional.of(ride));
        when(bookingRepo.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b != null && b.getId() == null)
                setId(b, 100L);
            return b;
        });
    }

    // ── bookRide ──────────────────────────────────────────────────────────────

    @Test
    void testBookRide_success() {
        Booking result = service.bookRide(2L, 10L, 2);

        assertEquals(2, result.getSeatsBooked());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(bookingRepo, times(1)).save(any(Booking.class));
    }

    @Test
    void testBookRide_zeroSeats_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 10L, 0));
    }

    @Test
    void testBookRide_notEnoughSeats_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 10L, 10));
    }

    @Test
    void testBookRide_rideNotFound_throwsException() {
        when(rideRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 99L, 1));
    }

    @Test
    void testBookRide_cancelledRide_throwsException() {
        ride.setStatus(RideStatus.CANCELLED);
        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 10L, 1));
    }

    @Test
    void testBookRide_completedRide_throwsException() {
        ride.setStatus(RideStatus.COMPLETED);
        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 10L, 1));
    }

    @Test
    void testBookRide_driverBookingOwnRide_throwsException() {
        // User 1 is the driver of ride 10
        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        // Make a RIDER-role user with same ID as driver (simulate driver trying to
        // impersonate rider)
        User driverAsRider = makeUser(1L, UserRole.RIDER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(driverAsRider));

        assertThrows(IllegalArgumentException.class, () -> service.bookRide(1L, 10L, 1));
    }

    @Test
    void testBookRide_bannedRider_throwsException() {
        rider.setBanned(true);
        assertThrows(IllegalArgumentException.class, () -> service.bookRide(2L, 10L, 1));
    }

    @Test
    void testBookRide_exactSeats_setsStatusToBooked() {
        service.bookRide(2L, 10L, 3); // all 3 seats
        assertEquals(RideStatus.BOOKED, ride.getStatus());
    }

    // ── cancelBooking ─────────────────────────────────────────────────────────

    @Test
    void testCancelBooking_success() {
        Booking booking = new Booking(rider, ride, 1);
        setId(booking, 50L);
        when(bookingRepo.findById(50L)).thenReturn(Optional.of(booking));
        when(bookingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking result = service.cancelBooking(50L);

        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelBooking_alreadyCancelled_throwsException() {
        Booking booking = new Booking(rider, ride, 1);
        setId(booking, 51L);
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepo.findById(51L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class, () -> service.cancelBooking(51L));
    }

    @Test
    void testCancelBooking_fullyBookedRide_reopensRide() {
        Ride fullRide = makeRide(20L, driver, 1, RideStatus.BOOKED);
        Booking booking = new Booking(rider, fullRide, 1);
        setId(booking, 52L);
        when(bookingRepo.findById(52L)).thenReturn(Optional.of(booking));
        when(bookingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.cancelBooking(52L);

        assertEquals(RideStatus.PUBLISHED, fullRide.getStatus());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User makeUser(Long id, UserRole role) {
        User u = new User("User" + id, "u" + id + "@test.com", "hash", "000", role);
        setId(u, id);
        return u;
    }

    private Ride makeRide(Long id, User driver, int seats, RideStatus status) {
        Ride r = new Ride("A", "B", seats, 100.0, LocalDateTime.now().plusDays(1), driver);
        setId(r, id);
        r.setStatus(status);
        return r;
    }

    private static void setId(User u, Long id) {
        u.setUserId(id);
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
