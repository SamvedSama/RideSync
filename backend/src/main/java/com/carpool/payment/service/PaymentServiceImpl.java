package com.carpool.payment.service;

import com.carpool.model.Booking;
import com.carpool.model.BookingStatus;
import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.Payment;
import com.carpool.payment.model.PaymentStatus;
import com.carpool.payment.repository.PaymentRepository;
import com.carpool.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PaymentServiceImpl — concrete implementation of PaymentService.
 *
 * Design Principle: Dependency Inversion Principle (DIP)
 * This class depends on:
 *   - PaymentRepository (abstraction via Spring Data)
 *   - MockPaymentGateway (injected, can be swapped)
 *   - BookingRepository (abstraction via Spring Data)
 *
 * The Facade (PaymentFacade) depends only on the PaymentService interface,
 * not on this class directly.
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final MockPaymentGateway gateway;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                               BookingRepository bookingRepository,
                               MockPaymentGateway gateway) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.gateway = gateway;
    }

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Booking not found: " + request.getBookingId()));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot pay for a cancelled booking");
        }

        // Prevent duplicate payments
        paymentRepository.findByBookingId(booking.getId()).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                throw new IllegalArgumentException("Payment already completed for this booking");
            }
        });

        Payment payment = new Payment(booking, request.getAmount(), request.getMethod());
        Payment saved = paymentRepository.save(payment);

        return toResponse(saved);
    }

    @Override
    public PaymentResponse processPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Payment already processed");
        }
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("Payment was already refunded");
        }

        try {
            String txnRef = gateway.charge(payment.getAmount(), payment.getMethod().name());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionRef(txnRef);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new IllegalArgumentException("Payment failed: " + e.getMessage());
        }

        payment.setUpdatedAt(LocalDateTime.now());
        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Only successful payments can be refunded");
        }

        gateway.refund(payment.getTransactionRef());

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse getPaymentByBooking(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payment found for booking: " + bookingId));
        return toResponse(payment);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setBookingId(payment.getBooking().getId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setMethod(payment.getMethod());
        response.setTransactionRef(payment.getTransactionRef());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
