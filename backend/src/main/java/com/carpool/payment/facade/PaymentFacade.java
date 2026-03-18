package com.carpool.payment.facade;

import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.Payment;
import com.carpool.payment.service.PaymentService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PaymentFacade — Facade Design Pattern (Member 4)
 *
 * PURPOSE:
 * Provides a single, simplified entry point for all payment-related operations.
 * The controller and any other caller only need to interact with this Facade —
 * they are shielded from the complexity of:
 *   - Payment initiation logic
 *   - Gateway charging steps
 *   - Refund handling
 *   - Status transitions
 *
 * DESIGN PRINCIPLE: Dependency Inversion Principle (DIP)
 * This Facade depends on the PaymentService *interface*, not the concrete
 * PaymentServiceImpl. This means the underlying implementation can be swapped
 * (e.g. real gateway) without any changes to this class or the controller.
 *
 * USAGE:
 *   PaymentFacade.pay(request)  — initiates + processes payment in one call
 *   PaymentFacade.refund(id)    — refunds a successful payment
 *   PaymentFacade.status(id)    — gets payment status for a booking
 *   PaymentFacade.listAll()     — admin: list all payments
 */
@Component
public class PaymentFacade {

    private final PaymentService paymentService;

    // DIP: depends on abstraction (PaymentService), not concrete class
    public PaymentFacade(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * One-step payment: initiates a payment record then immediately processes it.
     * Callers do not need to know about the two-step initiate → process flow.
     */
    public PaymentResponse pay(PaymentRequest request) {
        PaymentResponse initiated = paymentService.initiatePayment(request);
        return paymentService.processPayment(initiated.getPaymentId());
    }

    /**
     * Refunds a payment by its payment ID.
     */
    public PaymentResponse refund(Long paymentId) {
        return paymentService.refundPayment(paymentId);
    }

    /**
     * Retrieves payment details for a given booking.
     */
    public PaymentResponse getPaymentForBooking(Long bookingId) {
        return paymentService.getPaymentByBooking(bookingId);
    }

    /**
     * Returns all payments — used by admin management.
     */
    public List<Payment> listAllPayments() {
        return paymentService.getAllPayments();
    }
}
