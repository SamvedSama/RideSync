package com.carpool.service;

import com.carpool.model.Payment;
import com.carpool.model.PaymentStatus;
import com.carpool.model.PaymentMethod;
import com.carpool.model.Booking;
import com.carpool.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Payment Service for handling payment operations
 * Follows Single Responsibility Principle
 */
@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * Process payment for a booking
     */
    public Payment processPayment(Long bookingId, double amount, String paymentMethod) {
        // In a real application, this would integrate with a payment gateway
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PROCESSING);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Simulate payment processing and mark as completed
        // In a real application, this would be async and depend on payment gateway response
        savedPayment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(savedPayment);
    }
    
    /**
     * Get payment by booking
     */
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByBooking(Booking booking) {
        return paymentRepository.findByBooking(booking);
    }
    
    /**
     * Get payment by booking ID
     */
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream().findFirst();
    }
    
    /**
     * Complete payment
     */
    public Payment completePayment(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.COMPLETED);
            return paymentRepository.save(payment);
        }
        throw new IllegalArgumentException("Payment not found");
    }
    
    /**
     * Fail payment
     */
    public Payment failPayment(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.FAILED);
            return paymentRepository.save(payment);
        }
        throw new IllegalArgumentException("Payment not found");
    }
    
    /**
     * Refund payment
     */
    public Payment refundPayment(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.REFUNDED);
            return paymentRepository.save(payment);
        }
        throw new IllegalArgumentException("Payment not found");
    }
    
    /**
     * Process passenger payment with specific payment method
     */
    public Payment processPassengerPayment(Long bookingId, PaymentMethod paymentMethod, Long passengerId) {
        // In a real application, this would integrate with a payment gateway
        Payment payment = new Payment();
        payment.setAmount(calculateAmount(bookingId));
        payment.setPaymentMethod(paymentMethod.name());
        payment.setStatus(PaymentStatus.PROCESSING);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Simulate payment processing and mark as completed
        // In a real application, this would be async and depend on payment gateway response
        savedPayment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(savedPayment);
    }

    /**
     * Get payments for a specific driver
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForDriver(Long driverId) {
        // This would need to be implemented based on the relationship between payments and drivers
        // For now, return all completed payments as a placeholder
        return paymentRepository.findByStatus(PaymentStatus.COMPLETED);
    }

    /**
     * Get payments for a specific ride
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForRide(Long rideId, Long driverId) {
        // This would need to be implemented based on the relationship between payments and rides
        // For now, return all completed payments as a placeholder
        return paymentRepository.findByStatus(PaymentStatus.COMPLETED);
    }

    /**
     * Calculate amount for a booking (placeholder implementation)
     */
    private double calculateAmount(Long bookingId) {
        // In a real application, this would fetch the booking and calculate based on fare
        return 100.0; // Placeholder amount
    }

    /**
     * Get all payments by status
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
}
