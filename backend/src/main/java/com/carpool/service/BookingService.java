package com.carpool.service;

import com.carpool.model.Booking;
import com.carpool.model.Ride;
import com.carpool.model.RideStatus;

import java.util.List;

public interface BookingService {
    Booking bookRide(Long riderId, Long rideId, int seats);
    Booking cancelBooking(Long bookingId);
    List<Booking> getBookingsByRider(Long riderId);
    List<Booking> getBookingsForRide(Long rideId);
    
    // New methods for history functionality
    List<Booking> getBookingsByUser(Long userId);
    List<Booking> getBookingsForRides(List<Ride> rides);

    Booking refreshBookingState(Long bookingId);
    Booking startRideWithOtp(Long rideId, String otp, Long driverId);
    
    // Update booking statuses when ride status changes
    void updateBookingStatusesForRide(Long rideId, RideStatus rideStatus);
}