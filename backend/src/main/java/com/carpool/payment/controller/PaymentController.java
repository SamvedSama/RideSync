package com.carpool.payment.controller;

import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.facade.PaymentFacade;
import com.carpool.payment.model.Payment;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PaymentController — REST API for Payment Processing (Member 4)
 *
 * All payment operations are routed through the PaymentFacade.
 * The controller knows nothing about service internals or gateway logic.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    public PaymentController(PaymentFacade paymentFacade) {
        this.paymentFacade = paymentFacade;
    }

    /**
     * POST /api/payments
     * Initiate and process a payment for a booking in one call.
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentFacade.pay(request));
    }

    /**
     * PUT /api/payments/{paymentId}/refund
     * Refund a successful payment.
     */
    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentFacade.refund(paymentId));
    }

    /**
     * GET /api/payments/booking/{bookingId}
     * Get payment details for a specific booking.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentFacade.getPaymentForBooking(bookingId));
    }

    /**
     * GET /api/payments
     * List all payments (admin use).
     */
    @GetMapping
    public ResponseEntity<List<Payment>> listAll() {
        return ResponseEntity.ok(paymentFacade.listAllPayments());
    }
}
