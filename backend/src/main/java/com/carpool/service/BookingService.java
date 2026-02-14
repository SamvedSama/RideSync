package com.carpool.service;

import com.carpool.model.Booking;

public interface BookingService {

    Booking bookRide(Long riderId, Long rideId, int seats);

    Booking cancelBooking(Long bookingId);
}
