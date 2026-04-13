package com.carpool.repository;

import com.carpool.model.Rating;
import com.carpool.model.User;
import com.carpool.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRatedUser(User ratedUser);
    List<Rating> findByRatingUser(User ratingUser);
    Optional<Rating> findByBooking(Booking booking);
    List<Rating> findByBookingId(Long bookingId);
}
