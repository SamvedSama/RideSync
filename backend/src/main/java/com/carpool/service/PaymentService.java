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
 *
 * DESIGN PATTERNS USED:
 * 1. Service Layer Pattern - Encapsulates payment business logic
 *    - Acts as intermediary between controllers and repositories
 *    - Centralizes payment validation, processing, and status updates
 *    - Enables loose coupling and reusability across controllers
 *
 * 2. Repository Pattern - Uses PaymentRepository and BookingRepository
 *    - Abstracts data access operations
 *    - Decouples service layer from database implementation
 *
 * 3. DTO Pattern - Returns Payment domain objects (could be improved with DTOs)
 *    - Separates internal domain model from API contract
 *
 * DESIGN PRINCIPLES:
 * 1. Single Responsibility Principle (SRP)
 *    - Each method handles one specific payment operation
 *    - Payment processing isolated from booking logic
 *
 * 2. Dependency Inversion Principle (DIP)
 *    - Depends on repository abstractions, not concrete implementations
 *
 * CREATIONAL PATTERNS:
 * - Singleton Pattern: Spring @Service annotation creates singleton bean
 *   - Spring container ensures single instance per application context
 *   - Thread-safe transaction management
 *
 * - No Factory/Builder/Prototype patterns used
 *   - Payment objects created directly with 'new Payment()'
 *   - Could be improved with PaymentFactory for different payment types
 */
@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private com.carpool.repository.BookingRepository bookingRepository;
    
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
        return paymentRepository.findByBooking_Id(bookingId).stream().findFirst();
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
        System.out.println("Processing passenger payment - bookingId: " + bookingId + ", passengerId: " + passengerId);
        
        // Fetch the booking
        com.carpool.model.Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
        
        System.out.println("Booking found: " + booking.getId() + ", rider: " + booking.getRider());
        
        // Verify the booking belongs to the passenger
        if (booking.getRider() == null) {
            throw new IllegalArgumentException("Booking has no rider associated. Booking ID: " + bookingId);
        }
        
        if (booking.getRider().getUserId() == null) {
            throw new IllegalArgumentException("Booking rider has no userId set. Booking ID: " + bookingId);
        }
        
        if (!booking.getRider().getUserId().equals(passengerId)) {
            throw new IllegalArgumentException("Booking does not belong to this passenger. Expected rider ID: " + booking.getRider().getUserId() + ", Got passenger ID: " + passengerId);
        }
        
        // In a real application, this would integrate with a payment gateway
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalFare());
        payment.setPaymentMethod(paymentMethod.name());
        payment.setStatus(PaymentStatus.PROCESSING);
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(java.time.LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);
        System.out.println("Payment saved as COMPLETED: " + savedPayment.getId());
        
        // Link payment to booking and mark booking as PAID
        booking.setPayment(savedPayment);
        booking.setStatus(com.carpool.model.BookingStatus.PAID);
        bookingRepository.save(booking);
        System.out.println("Booking updated with payment and marked PAID");
        
        return savedPayment;
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
     * Confirm payment and update booking status to PAID
     */
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(java.time.LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);
        
        // Update booking status to PAID
        com.carpool.model.Booking booking = payment.getBooking();
        booking.setStatus(com.carpool.model.BookingStatus.PAID);
        bookingRepository.save(booking);
        
        return savedPayment;
    }
    
    /**
     * Reject payment
     */
    public Payment rejectPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        
        payment.setStatus(PaymentStatus.FAILED);
        payment.setProcessedAt(java.time.LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    /**
     * Get pending payments (PROCESSING status) for a driver's rides
     */
    @Transactional(readOnly = true)
    public List<Payment> getPendingPaymentsForDriver(Long driverId) {
        // Get all bookings for rides owned by this driver
        List<com.carpool.model.Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getRide().getDriver().getUserId().equals(driverId))
                .toList();
        
        // Get payments for these bookings that are in PROCESSING status
        return bookings.stream()
                .map(b -> b.getPayment())
                .filter(p -> p != null && p.getStatus() == PaymentStatus.PROCESSING)
                .toList();
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
