package com.carpool.controller;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.User;
import com.carpool.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService service;

    public RideController(RideService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('DRIVER') or hasRole('PASSENGER')")
    public ResponseEntity<Ride> createRide(@AuthenticationPrincipal User currentUser,
            @Valid @RequestBody Ride ride) {
        return ResponseEntity.ok(service.createRide(currentUser.getUserId(), ride));
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Ride>> searchRides(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination) {

        if ((source == null || source.isBlank()) && (destination == null || destination.isBlank())) {
            return ResponseEntity.ok(service.getAvailableRides());
        }

        if (source == null || source.isBlank() || destination == null || destination.isBlank()) {
            return ResponseEntity.ok(service.getAvailableRides());
        }

        return ResponseEntity.ok(service.searchRides(source, destination));
    }

    @GetMapping("/{rideId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Ride> getRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(service.getRide(rideId));
    }

    @GetMapping("/my-rides")
    @PreAuthorize("hasRole('DRIVER')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Ride>> getMyRides(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getRidesByDriver(currentUser.getUserId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(service.getAllRides());
    }

    @PutMapping("/{rideId}/status")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<Ride> updateRideStatus(@PathVariable Long rideId,
            @RequestParam RideStatus status,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.updateRideStatus(rideId, status));
    }

    @PutMapping("/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Ride> startRide(@PathVariable Long rideId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.updateRideStatus(rideId, RideStatus.IN_PROGRESS));
    }

    @PutMapping("/{rideId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Ride> completeRide(@PathVariable Long rideId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.updateRideStatus(rideId, RideStatus.COMPLETED));
    }

    @PutMapping("/{rideId}/cancel")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<Ride> cancelRide(@PathVariable Long rideId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.updateRideStatus(rideId, RideStatus.CANCELLED));
    }
}