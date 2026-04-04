package com.carpool.repository;

import com.carpool.model.Booking;
import com.carpool.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRiderUserId(Long riderId);
    List<Booking> findByRideId(Long rideId);
    Optional<Booking> findByRideIdAndStatus(Long rideId, BookingStatus status);
}