package com.carpool.controller;

import com.carpool.model.Ride;
import com.carpool.service.RideService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService service;

    public RideController(RideService service) {
        this.service = service;
    }

    // Create Ride
    @PostMapping("/{driverId}")
    public Ride createRide(@PathVariable Long driverId,
            @Valid @RequestBody Ride ride) {
        return service.createRide(driverId, ride);
    }

    // Search Ride
    @GetMapping("/search")
    public List<Ride> searchRides(@RequestParam String source,
            @RequestParam String destination) {
        return service.searchRides(source, destination);
    }

    // Get Ride by ID
    @GetMapping("/{rideId}")
    public Ride getRide(@PathVariable Long rideId) {
        return service.getRide(rideId);
    }
}
