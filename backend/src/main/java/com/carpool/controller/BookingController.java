package com.carpool.controller;

import com.carpool.model.Booking;
import com.carpool.service.BookingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking bookRide(@RequestParam Long riderId,
                            @RequestParam Long rideId,
                            @RequestParam int seats) {
        return bookingService.bookRide(riderId, rideId, seats);
    }

    @PutMapping("/{bookingId}/cancel")
    public Booking cancel(@PathVariable Long bookingId) {
        return bookingService.cancelBooking(bookingId);
    }
}
