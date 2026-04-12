package com.carpool.repository;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

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

    // Additional methods needed by services
    List<Ride> findByDriverIdOrderByDepartureTimeDesc(Long driverId);
    long countByStatus(RideStatus status);
    
    @Query("SELECT r.source, r.destination, COUNT(*) as count FROM Ride r GROUP BY r.source, r.destination ORDER BY count DESC")
    List<Object[]> findPopularRoutes();
    
    @Query("SELECT r FROM Ride r ORDER BY r.departureTime DESC")
    List<Ride> findTop10ByOrderByDepartureTimeDesc(Pageable pageable);
}