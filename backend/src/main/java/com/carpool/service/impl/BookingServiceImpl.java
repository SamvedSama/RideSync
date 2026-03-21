package com.carpool.service.impl;

import com.carpool.model.*;
import com.carpool.repository.*;
import com.carpool.service.BookingService;
import com.carpool.service.strategy.FareStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalArgumentException("Ride not available for booking");
        }

        if (ride.getStatus() != RideStatus.PUBLISHED && ride.getStatus() != RideStatus.BOOKED) {
            throw new IllegalArgumentException("Ride not open for booking");
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

        // Prevent driver from booking their own ride
        if (ride.getDriver().getUserId().equals(riderId)) {
            throw new IllegalArgumentException("Drivers cannot book their own rides");
        }

        fareStrategy.calculateTotalFare(ride.getFarePerSeat(), seats);

        Booking booking = new Booking(rider, ride, seats);
        ride.addBooking(booking);

        if (ride.getAvailableSeats() == 0) {
            ride.setStatus(RideStatus.BOOKED);
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Ride ride = booking.getRide();

        if (ride.getStatus() == RideStatus.BOOKED && ride.getAvailableSeats() > 0) {
            ride.setStatus(RideStatus.PUBLISHED);
        }

        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getBookingsByRider(Long riderId) {
        return bookingRepository.findByRiderUserId(riderId);
    }

    @Override
    public List<Booking> getBookingsForRide(Long rideId) {
        return bookingRepository.findByRideId(rideId);
    }
}
