package com.carpool.controller;

import com.carpool.model.Payment;
import com.carpool.model.PaymentStatus;
import com.carpool.model.PaymentMethod;
import com.carpool.model.User;
import com.carpool.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    @PreAuthorize("hasRole('DRIVER') or hasRole('PASSENGER')")
    public ResponseEntity<Payment> processPayment(
            @RequestParam Long bookingId,
            @RequestParam double amount,
            @RequestParam PaymentMethod paymentMethod,
            @AuthenticationPrincipal User currentUser) {
        try {
            Payment payment = paymentService.processPayment(bookingId, amount, paymentMethod.name());
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process payment: " + e.getMessage());
        }
    }

    @PostMapping("/passenger/pay")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Payment> passengerPayment(
            @RequestParam Long bookingId,
            @RequestParam PaymentMethod paymentMethod,
            @AuthenticationPrincipal User currentUser) {
        try {
            Payment payment = paymentService.processPassengerPayment(bookingId, paymentMethod, currentUser.getUserId());
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process passenger payment: " + e.getMessage());
        }
    }

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethod>> getAvailablePaymentMethods() {
        return ResponseEntity.ok(List.of(PaymentMethod.values()));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('PASSENGER')")
    public ResponseEntity<Payment> getPaymentByBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User currentUser) {
        return paymentService.getPaymentByBookingId(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{paymentId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Payment> completePayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.completePayment(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to complete payment: " + e.getMessage());
        }
    }

    @PostMapping("/{paymentId}/fail")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Payment> failPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.failPayment(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark payment as failed: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('DRIVER') or hasRole('PASSENGER')")
    public ResponseEntity<List<Payment>> getMyPayments(@AuthenticationPrincipal User currentUser) {
        // This would need to be implemented in PaymentService
        // For now, return all payments (would need filtering by user in real implementation)
        List<Payment> payments = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/driver/payments")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Payment>> getDriverPayments(@AuthenticationPrincipal User currentUser) {
        try {
            List<Payment> payments = paymentService.getPaymentsForDriver(currentUser.getUserId());
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver payments: " + e.getMessage());
        }
    }

    @GetMapping("/driver/ride/{rideId}/payments")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Payment>> getRidePayments(@PathVariable Long rideId,
            @AuthenticationPrincipal User currentUser) {
        try {
            List<Payment> payments = paymentService.getPaymentsForRide(rideId, currentUser.getUserId());
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ride payments: " + e.getMessage());
        }
    }
}
