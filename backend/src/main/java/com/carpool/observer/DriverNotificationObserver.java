package com.carpool.observer;

import com.carpool.model.Booking;
import com.carpool.model.BookingStatus;
import com.carpool.model.Ride;
import com.carpool.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DriverNotificationObserver implements RideStatusObserver {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public void onRideStatusChanged(Ride ride) {
        // Check for new confirmed bookings for this ride
        bookingRepository.findByRideId(ride.getId()).stream()
            .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
            .forEach(booking -> {
                System.out.println("NOTIFICATION: Driver " + ride.getDriver().getName() + 
                    " - New booking accepted!");
                System.out.println("  Ride ID: " + ride.getId());
                System.out.println("  Passenger: " + booking.getRider().getName());
                System.out.println("  Seats booked: " + booking.getSeatsBooked());
                System.out.println("  Total fare: $" + booking.getTotalFare());
                System.out.println("  Booking time: " + booking.getCreatedAt());
                System.out.println("  Status: " + booking.getStatus());
                System.out.println("---");
            });
        
        System.out.println("Driver notified: Ride " + ride.getId() + 
            " status updated to " + ride.getStatus());
    }
}