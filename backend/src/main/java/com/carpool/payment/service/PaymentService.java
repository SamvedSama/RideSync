package com.carpool.payment.service;

import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.Payment;

import java.util.List;

/**
 * PaymentService interface.
 *
 * Design Principle: Dependency Inversion Principle (DIP)
 * High-level modules (PaymentFacade, PaymentController) depend on this abstraction,
 * not on the concrete PaymentServiceImpl. This allows swapping implementations
 * (e.g. real gateway, mock gateway) without changing callers.
 */
public interface PaymentService {

    PaymentResponse initiatePayment(PaymentRequest request);

    PaymentResponse processPayment(Long paymentId);

    PaymentResponse refundPayment(Long paymentId);

    PaymentResponse getPaymentByBooking(Long bookingId);

    List<Payment> getAllPayments();
}
