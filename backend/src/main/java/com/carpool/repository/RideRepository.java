package com.carpool.repository;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);

    @Query("SELECT DISTINCT r FROM Ride r LEFT JOIN FETCH r.bookings " +
            "WHERE LOWER(r.source) = LOWER(:source) " +
            "AND LOWER(r.destination) = LOWER(:destination) " +
            "AND r.status IN :statuses")
    List<Ride> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndStatuses(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("statuses") List<RideStatus> statuses);

    @Query("SELECT DISTINCT r FROM Ride r LEFT JOIN FETCH r.bookings " +
            "WHERE r.status IN :statuses")
    List<Ride> findByStatuses(@Param("statuses") List<RideStatus> statuses);

    @Query("SELECT DISTINCT r FROM Ride r LEFT JOIN FETCH r.bookings WHERE r.driver.userId = :driverId")
    List<Ride> findByDriverUserId(@Param("driverId") Long driverId);

    @Query("SELECT DISTINCT r FROM Ride r LEFT JOIN FETCH r.bookings WHERE r.id = :id")
    Optional<Ride> findByIdWithBookings(@Param("id") Long id);
}