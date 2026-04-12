package com.carpool.service;

import com.carpool.model.Booking;
import com.carpool.model.BookingStatus;
import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.User;
import com.carpool.observer.DriverNotificationObserver;
import com.carpool.observer.RideStatusManager;
import com.carpool.repository.BookingRepository;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Booking Service Implementation
 * Follows Single Responsibility Principle
 */
@Service
@Transactional
public class BookingServiceImpl implements BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RideRepository rideRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RideStatusManager rideStatusManager;
    
    @Autowired
    private DriverNotificationObserver driverNotificationObserver;
    
    @PostConstruct
    public void initializeObservers() {
        rideStatusManager.addObserver(driverNotificationObserver);
    }
    
    @Override
    public Booking bookRide(Long riderId, Long rideId, int seats) {
        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        
        if (ride.getAvailableSeats() < seats) {
            throw new IllegalArgumentException("Not enough seats available");
        }
        
        Booking booking = new Booking(rider, ride, seats);
        booking.setStatus(BookingStatus.CONFIRMED);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Notify driver about the new booking
        rideStatusManager.notifyObservers(ride);
        
        return savedBooking;
    }
    
    @Override
    public Booking cancelBooking(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(BookingStatus.CANCELLED);
            return bookingRepository.save(booking);
        }
        throw new IllegalArgumentException("Booking not found");
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByRider(Long riderId) {
        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return bookingRepository.findByRiderOrderByCreatedAtDesc(rider);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsForRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        return bookingRepository.findByRideId(rideId);
    }
    
    @Override
    public Booking refreshBookingState(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.refreshWaitingCharge();
            return bookingRepository.save(booking);
        }
        throw new IllegalArgumentException("Booking not found");
    }
    
    @Override
    public Booking startRideWithOtp(Long rideId, String otp, Long driverId) {
        List<Booking> bookings = bookingRepository.findByRideId(rideId);
        for (Booking booking : bookings) {
            if (booking.getStartOtp() != null && booking.getStartOtp().equals(otp)) {
                booking.setStatus(BookingStatus.IN_PROGRESS);
                return bookingRepository.save(booking);
            }
        }
        throw new IllegalArgumentException("Invalid OTP or booking not found");
    }
    
    @Override
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByRiderUserId(userId);
    }
    
    @Override
    public List<Booking> getBookingsForRides(List<Ride> rides) {
        return rides.stream()
                .flatMap(ride -> bookingRepository.findByRideId(ride.getId()).stream())
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Update booking statuses when ride status changes
     */
    @Override
    public void updateBookingStatusesForRide(Long rideId, RideStatus rideStatus) {
        List<Booking> bookings = bookingRepository.findByRideId(rideId);
        
        for (Booking booking : bookings) {
            switch (rideStatus) {
                case IN_PROGRESS:
                    if (booking.getStatus() == BookingStatus.CONFIRMED) {
                        booking.setStatus(BookingStatus.IN_PROGRESS);
                    }
                    break;
                case COMPLETED:
                    if (booking.getStatus() == BookingStatus.CONFIRMED || 
                        booking.getStatus() == BookingStatus.IN_PROGRESS) {
                        booking.setStatus(BookingStatus.COMPLETED);
                    }
                    break;
                case CANCELLED:
                    booking.setStatus(BookingStatus.CANCELLED);
                    break;
                default:
                    // No status change needed for other statuses
                    break;
            }
            bookingRepository.save(booking);
        }
    }
}
