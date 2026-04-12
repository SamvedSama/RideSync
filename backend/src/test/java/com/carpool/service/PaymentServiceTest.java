package com.carpool.service;

import com.carpool.model.Payment;
import com.carpool.model.PaymentStatus;
import com.carpool.model.PaymentMethod;
import com.carpool.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setAmount(100.0);
        testPayment.setPaymentMethod("CREDIT_CARD");
        testPayment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void testProcessPayment() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        Payment result = paymentService.processPayment(1L, 100.0, "CREDIT_CARD");

        assertNotNull(result);
        assertEquals(100.0, result.getAmount());
        assertEquals("CREDIT_CARD", result.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testProcessPassengerPayment() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        Payment result = paymentService.processPassengerPayment(1L, PaymentMethod.CREDIT_CARD, 1L);

        assertNotNull(result);
        assertEquals(100.0, result.getAmount()); // Mocked calculateAmount returns 100.0
        assertEquals("CREDIT_CARD", result.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testCompletePayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(testPayment)).thenReturn(testPayment);

        Payment result = paymentService.completePayment(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getProcessedAt());
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void testCompletePaymentNotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.completePayment(1L);
        });
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void testFailPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(testPayment)).thenReturn(testPayment);

        Payment result = paymentService.failPayment(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void testRefundPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(testPayment)).thenReturn(testPayment);

        Payment result = paymentService.refundPayment(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.REFUNDED, result.getStatus());
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void testGetPaymentsByStatus() {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(payments);

        List<Payment> result = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PaymentStatus.PENDING, result.get(0).getStatus()); // testPayment has PENDING status
        verify(paymentRepository).findByStatus(PaymentStatus.COMPLETED);
    }

    @Test
    void testGetPaymentsForDriver() {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(payments);

        List<Payment> result = paymentService.getPaymentsForDriver(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByStatus(PaymentStatus.COMPLETED);
    }

    @Test
    void testGetPaymentsForRide() {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(payments);

        List<Payment> result = paymentService.getPaymentsForRide(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByStatus(PaymentStatus.COMPLETED);
    }

    @Test
    void testGetPaymentByBookingId() {
        when(paymentRepository.findByBookingId(1L)).thenReturn(Arrays.asList(testPayment));

        Optional<Payment> result = paymentService.getPaymentByBookingId(1L);

        assertTrue(result.isPresent());
        assertEquals(testPayment, result.get());
        verify(paymentRepository).findByBookingId(1L);
    }

    @Test
    void testGetPaymentByBookingIdNotFound() {
        when(paymentRepository.findByBookingId(1L)).thenReturn(Arrays.asList());

        Optional<Payment> result = paymentService.getPaymentByBookingId(1L);

        assertFalse(result.isPresent());
        verify(paymentRepository).findByBookingId(1L);
    }
}
