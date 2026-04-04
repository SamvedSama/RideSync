package com.carpool.service.impl;

import com.carpool.model.*;
import com.carpool.repository.*;
import com.carpool.service.BookingService;
import com.carpool.service.strategy.FareStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final FareStrategy fareStrategy;

    public BookingServiceImpl(BookingRepository bookingRepository,
                               RideRepository rideRepository,
                               UserRepository userRepository,
                               FareStrategy fareStrategy) {
        this.bookingRepository = bookingRepository;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.fareStrategy = fareStrategy;
    }

    @Override
    public Booking bookRide(Long riderId, Long rideId, int seats) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Seats must be greater than zero");
        }

        Ride ride = rideRepository.findByIdWithBookings(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalArgumentException("Ride not available for booking");
        }

        if (ride.getStatus() == RideStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Ride already started");
        }

        if (ride.getAvailableSeats() < seats) {
            throw new IllegalArgumentException("Not enough seats available");
        }

        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (rider.isBanned()) {
            throw new IllegalArgumentException("Your account has been suspended");
        }

        if (rider.getRole() != UserRole.RIDER) {
            throw new IllegalArgumentException("Only riders can book rides");
        }

        if (ride.getDriver().getUserId().equals(riderId)) {
            throw new IllegalArgumentException("Drivers cannot book their own rides");
        }

        Booking booking = new Booking(rider, ride, seats);
        booking.generateOtp(generateOtp());

        fareStrategy.calculateTotalFare(ride.getFarePerSeat(), seats, 0.0);

        ride.addBooking(booking);
        ride.setStatus(RideStatus.BOOKED);

        rideRepository.save(ride);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking already cancelled");
        }

        if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a ride that has already started or completed");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Ride ride = booking.getRide();
        syncRideStatusFromBookings(ride);

        rideRepository.save(ride);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByRider(Long riderId) {
        List<Booking> bookings = bookingRepository.findByRiderUserId(riderId);
        bookings.forEach(Booking::refreshWaitingCharge);
        return bookings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsForRide(Long rideId) {
        List<Booking> bookings = bookingRepository.findByRideId(rideId);
        bookings.forEach(Booking::refreshWaitingCharge);
        return bookings;
    }

    @Override
    public Booking refreshBookingState(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.refreshWaitingCharge();
        return bookingRepository.save(booking);
    }

    @Override
    public Booking startRideWithOtp(Long rideId, String otp, Long driverId) {
        Ride ride = rideRepository.findByIdWithBookings(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        if (!ride.getDriver().getUserId().equals(driverId)) {
            throw new IllegalArgumentException("Only the ride driver can start this ride");
        }

        if (ride.getStatus() == RideStatus.CANCELLED || ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalArgumentException("Ride cannot be started");
        }

        Booking booking = ride.getBookings().stream()
                .filter(b -> b.getStatus() == BookingStatus.OTP_PENDING)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No OTP-pending booking found for this ride"));

        booking.refreshWaitingCharge();

        if (booking.getStartOtp() == null || !booking.getStartOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setStartOtp(null);

        ride.setStatus(RideStatus.IN_PROGRESS);

        rideRepository.save(ride);
        return bookingRepository.save(booking);
    }

    private void syncRideStatusFromBookings(Ride ride) {
        boolean hasInProgress = ride.getBookings().stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.IN_PROGRESS);

        boolean hasActivePending = ride.getBookings().stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.OTP_PENDING || b.getStatus() == BookingStatus.CONFIRMED);

        if (hasInProgress) {
            ride.setStatus(RideStatus.IN_PROGRESS);
        } else if (hasActivePending) {
            ride.setStatus(RideStatus.BOOKED);
        } else {
            ride.setStatus(RideStatus.PUBLISHED);
        }
    }

    private String generateOtp() {
        int otp = 100000 + new Random().nextInt(900000);
        return String.valueOf(otp);
    }
}