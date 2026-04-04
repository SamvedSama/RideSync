package com.carpool.controller;

import com.carpool.dto.BookingResponse;
import com.carpool.model.User;
import com.carpool.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<BookingResponse> bookRide(@AuthenticationPrincipal User currentUser,
                                                     @RequestParam Long rideId,
                                                     @RequestParam int seats) {
        return ResponseEntity.ok(BookingResponse.from(
                bookingService.bookRide(currentUser.getUserId(), rideId, seats)));
    }

    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('RIDER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long bookingId) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.cancelBooking(bookingId)));
    }

    @PutMapping("/{bookingId}/refresh")
    @PreAuthorize("hasRole('RIDER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> refreshBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.refreshBookingState(bookingId)));
    }

    @PostMapping("/ride/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<BookingResponse> startRideWithOtp(
            @PathVariable Long rideId,
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> payload) {

        String otp = payload.get("otp");
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("OTP is required");
        }

        return ResponseEntity.ok(
                BookingResponse.from(bookingService.startRideWithOtp(rideId, otp, currentUser.getUserId()))
        );
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bookingService.getBookingsByRider(currentUser.getUserId())
                .stream().map(BookingResponse::from).collect(Collectors.toList()));
    }

    @GetMapping("/ride/{rideId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsForRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(bookingService.getBookingsForRide(rideId)
                .stream().map(BookingResponse::from).collect(Collectors.toList()));
    }
}