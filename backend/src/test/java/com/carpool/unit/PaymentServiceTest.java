package com.carpool.unit;

import com.carpool.model.*;
import com.carpool.payment.dto.PaymentRequest;
import com.carpool.payment.dto.PaymentResponse;
import com.carpool.payment.model.Payment;
import com.carpool.payment.model.PaymentMethod;
import com.carpool.payment.model.PaymentStatus;
import com.carpool.payment.repository.PaymentRepository;
import com.carpool.payment.service.MockPaymentGateway;
import com.carpool.payment.service.PaymentServiceImpl;
import com.carpool.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private BookingRepository bookingRepository;
    private MockPaymentGateway gateway;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = Mockito.mock(PaymentRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        // Use real gateway — no mocking needed
        gateway = new MockPaymentGateway();
        paymentService = new PaymentServiceImpl(paymentRepository, bookingRepository, gateway);
    }

    // ── initiatePayment ───────────────────────────────────────────────────────

    @Test
    void testInitiatePayment_success() {
        Booking booking = makeBooking(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PaymentRequest req = new PaymentRequest(1L, PaymentMethod.UPI, 300.0);
        PaymentResponse response = paymentService.initiatePayment(req);

        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals(300.0, response.getAmount());
        assertEquals(PaymentMethod.UPI, response.getMethod());
    }

    @Test
    void testInitiatePayment_cancelledBooking_shouldFail() {
        Booking booking = makeBooking(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        PaymentRequest req = new PaymentRequest(1L, PaymentMethod.CASH, 100.0);
        assertThrows(IllegalArgumentException.class, () -> paymentService.initiatePayment(req));
    }

    @Test
    void testInitiatePayment_bookingNotFound_shouldFail() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        PaymentRequest req = new PaymentRequest(99L, PaymentMethod.CARD, 200.0);
        assertThrows(IllegalArgumentException.class, () -> paymentService.initiatePayment(req));
    }

    @Test
    void testInitiatePayment_duplicateSuccess_shouldFail() {
        Booking booking = makeBooking(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Payment existing = new Payment(booking, 300.0, PaymentMethod.UPI);
        existing.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(existing));

        PaymentRequest req = new PaymentRequest(1L, PaymentMethod.UPI, 300.0);
        assertThrows(IllegalArgumentException.class, () -> paymentService.initiatePayment(req));
    }

    // ── processPayment ────────────────────────────────────────────────────────

    @Test
    void testProcessPayment_success() {
        Booking booking = makeBooking(BookingStatus.CONFIRMED);
        Payment payment = new Payment(booking, 300.0, PaymentMethod.UPI);
        payment.setId(10L);

        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.processPayment(10L);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertNotNull(response.getTransactionRef());
        assertTrue(response.getTransactionRef().startsWith("TXN-UPI-"));
    }

    @Test
    void testProcessPayment_gatewayFailure_marksAsFailed() {
        Booking booking = makeBooking(BookingStatus.CONFIRMED);
        // Amount ending in .99 triggers gateway failure in MockPaymentGateway
        Payment payment = new Payment(booking, 100.99, PaymentMethod.CARD);
        payment.setId(11L);

        when(paymentRepository.findById(11L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(11L));
    }

    @Test
    void testProcessPayment_alreadyProcessed_shouldFail() {
        Booking booking = makeBooking(BookingStatus.CONFIRMED);
        Payment payment = new Payment(booking, 300.0, PaymentMethod.UPI);
        payment.setId(12L);
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(12L)).thenReturn(Optional.of(payment));

        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(12L));
    }

    // ── refundPayment ─────────────────────────────────────────────────────────

    @Test
    void testRefundPayment_success() {
        Booking booking = makeBooking(BookingStatus.CONFIRMED);
        Payment payment = new Payment(booking, 300.0, PaymentMethod.WALLET);
        payment.setId(20L);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("TXN-WALLET-XYZ");

        when(paymentRepository.findById(20L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.refundPayment(20L);
        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
    }

    @Test
    void testRefundPayment_notSuccess_shouldFail() {
        Booking booking = makeBooking(BookingStatus.CONFIRMED);
        Payment payment = new Payment(booking, 300.0, PaymentMethod.UPI);
        payment.setId(21L);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(21L)).thenReturn(Optional.of(payment));

        assertThrows(IllegalArgumentException.class, () -> paymentService.refundPayment(21L));
    }

    // ── MockPaymentGateway direct tests ───────────────────────────────────────

    @Test
    void testGateway_charge_returnsTransactionRef() {
        String txn = gateway.charge(500.0, "UPI");
        assertNotNull(txn);
        assertTrue(txn.startsWith("TXN-UPI-"));
    }

    @Test
    void testGateway_charge_amountEndingIn99_fails() {
        assertThrows(RuntimeException.class, () -> gateway.charge(100.99, "CARD"));
    }

    @Test
    void testGateway_refund_success() {
        assertTrue(gateway.refund("TXN-UPI-ABCD1234"));
    }

    @Test
    void testGateway_invalidAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> gateway.charge(-10.0, "CASH"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Booking makeBooking(BookingStatus status) {
        User rider = new User("Rider", "rider@test.com", "pass", "999", UserRole.RIDER);
        rider.setUserId(1L);
        Ride ride = new Ride();
        ride.setId(1L);
        Booking booking = new Booking(rider, ride, 3);
        booking.setId(1L);
        booking.setStatus(status);
        return booking;
    }
}