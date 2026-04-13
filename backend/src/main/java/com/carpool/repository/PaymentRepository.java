package com.carpool.repository;

import com.carpool.model.Payment;
import com.carpool.model.PaymentStatus;
import com.carpool.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByBooking_Id(Long bookingId);
}
