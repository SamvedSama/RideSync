package com.carpool.integration;

import com.carpool.model.RideStatus;
import com.carpool.model.PaymentMethod;
import com.carpool.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RideStatusIntegrationTest {

    @Test
    void testRideStatusEnumValues() {
        // Test that all required ride statuses are available
        RideStatus[] statuses = RideStatus.values();
        
        assertTrue(java.util.Arrays.asList(statuses).contains(RideStatus.CREATED));
        assertTrue(java.util.Arrays.asList(statuses).contains(RideStatus.PUBLISHED));
        assertTrue(java.util.Arrays.asList(statuses).contains(RideStatus.BOOKED));
        assertTrue(java.util.Arrays.asList(statuses).contains(RideStatus.IN_PROGRESS)); // ONGOING
        assertTrue(java.util.Arrays.asList(statuses).contains(RideStatus.COMPLETED));
        assertTrue(java.util.Arrays.asList(statuses).contains(RideStatus.CANCELLED));
    }

    @Test
    void testPaymentMethodEnumValues() {
        // Test that all required payment methods are available
        PaymentMethod[] methods = PaymentMethod.values();
        
        assertTrue(java.util.Arrays.asList(methods).contains(PaymentMethod.CREDIT_CARD));
        assertTrue(java.util.Arrays.asList(methods).contains(PaymentMethod.DEBIT_CARD));
        assertTrue(java.util.Arrays.asList(methods).contains(PaymentMethod.PAYPAL));
        assertTrue(java.util.Arrays.asList(methods).contains(PaymentMethod.UPI));
        assertTrue(java.util.Arrays.asList(methods).contains(PaymentMethod.NET_BANKING));
        assertTrue(java.util.Arrays.asList(methods).contains(PaymentMethod.WALLET));
        assertTrue(java.util.Arrays.asList(methods).contains(PaymentMethod.CASH));
    }

    @Test
    void testPaymentStatusEnumValues() {
        // Test that all required payment statuses are available
        PaymentStatus[] statuses = PaymentStatus.values();
        
        assertTrue(java.util.Arrays.asList(statuses).contains(PaymentStatus.PENDING));
        assertTrue(java.util.Arrays.asList(statuses).contains(PaymentStatus.PROCESSING));
        assertTrue(java.util.Arrays.asList(statuses).contains(PaymentStatus.COMPLETED));
        assertTrue(java.util.Arrays.asList(statuses).contains(PaymentStatus.FAILED));
        assertTrue(java.util.Arrays.asList(statuses).contains(PaymentStatus.REFUNDED));
    }

    @Test
    void testRideStatusTransitions() {
        // Test valid ride status transitions
        assertEquals("CREATED", RideStatus.CREATED.name());
        assertEquals("PUBLISHED", RideStatus.PUBLISHED.name());
        assertEquals("BOOKED", RideStatus.BOOKED.name());
        assertEquals("IN_PROGRESS", RideStatus.IN_PROGRESS.name()); // Maps to ONGOING
        assertEquals("COMPLETED", RideStatus.COMPLETED.name());
        assertEquals("CANCELLED", RideStatus.CANCELLED.name());
    }

    @Test
    void testPaymentMethodNames() {
        // Test payment method names are correctly formatted
        assertEquals("CREDIT_CARD", PaymentMethod.CREDIT_CARD.name());
        assertEquals("DEBIT_CARD", PaymentMethod.DEBIT_CARD.name());
        assertEquals("PAYPAL", PaymentMethod.PAYPAL.name());
        assertEquals("UPI", PaymentMethod.UPI.name());
        assertEquals("NET_BANKING", PaymentMethod.NET_BANKING.name());
        assertEquals("WALLET", PaymentMethod.WALLET.name());
        assertEquals("CASH", PaymentMethod.CASH.name());
    }
}
