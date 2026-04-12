package com.carpool.repository;

import com.carpool.model.Booking;
import com.carpool.model.BookingStatus;
import com.carpool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRiderUserId(Long riderId);
    List<Booking> findByRideId(Long rideId);
    Optional<Booking> findByRideIdAndStatus(Long rideId, BookingStatus status);
    
    // Additional methods needed by services
    List<Booking> findByRiderOrderByCreatedAtDesc(User rider);
    List<Booking> findByRiderAndStatusOrderByCreatedAtDesc(User rider, BookingStatus status);
    List<Booking> findByRiderId(Long riderId);
    List<Booking> findByRiderIdAndCreatedAtBetween(Long riderId, LocalDateTime start, LocalDateTime end);
    long countByStatus(BookingStatus status);
}