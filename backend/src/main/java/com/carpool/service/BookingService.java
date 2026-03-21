package com.carpool.service;

import com.carpool.model.Booking;

import java.util.List;

public interface BookingService {
    Booking bookRide(Long riderId, Long rideId, int seats);
    Booking cancelBooking(Long bookingId);
    List<Booking> getBookingsByRider(Long riderId);
    List<Booking> getBookingsForRide(Long rideId);
}
