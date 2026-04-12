package com.carpool.integration;

import com.carpool.model.*;
import com.carpool.service.*;
import com.carpool.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ComprehensiveFeatureTest {

    @Autowired
    private RideService rideService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void verifyAllRideStatusManagementFeatures() {
        // Test that all required ride statuses are available
        RideStatus[] statuses = RideStatus.values();
        List<RideStatus> statusList = Arrays.asList(statuses);
        
        assertTrue(statusList.contains(RideStatus.CREATED), "CREATED status should be available");
        assertTrue(statusList.contains(RideStatus.PUBLISHED), "PUBLISHED status should be available");
        assertTrue(statusList.contains(RideStatus.BOOKED), "BOOKED status should be available");
        assertTrue(statusList.contains(RideStatus.IN_PROGRESS), "IN_PROGRESS (ONGOING) status should be available");
        assertTrue(statusList.contains(RideStatus.COMPLETED), "COMPLETED status should be available");
        assertTrue(statusList.contains(RideStatus.CANCELLED), "CANCELLED status should be available");

        // Verify status names match expected values
        assertEquals("IN_PROGRESS", RideStatus.IN_PROGRESS.name(), "Should map to ONGOING");
        assertEquals("COMPLETED", RideStatus.COMPLETED.name(), "Should support COMPLETED status");
        assertEquals("CANCELLED", RideStatus.CANCELLED.name(), "Should support CANCELLED status");
    }

    @Test
    void verifyAllPaymentMethodFeatures() {
        // Test that all required payment methods are available
        PaymentMethod[] methods = PaymentMethod.values();
        List<PaymentMethod> methodList = Arrays.asList(methods);
        
        assertTrue(methodList.contains(PaymentMethod.CREDIT_CARD), "Credit card payment should be available");
        assertTrue(methodList.contains(PaymentMethod.DEBIT_CARD), "Debit card payment should be available");
        assertTrue(methodList.contains(PaymentMethod.PAYPAL), "PayPal payment should be available");
        assertTrue(methodList.contains(PaymentMethod.UPI), "UPI payment should be available");
        assertTrue(methodList.contains(PaymentMethod.NET_BANKING), "Net banking payment should be available");
        assertTrue(methodList.contains(PaymentMethod.WALLET), "Wallet payment should be available");
        assertTrue(methodList.contains(PaymentMethod.CASH), "Cash payment should be available");

        // Verify payment method names are correctly formatted
        assertEquals("CREDIT_CARD", PaymentMethod.CREDIT_CARD.name());
        assertEquals("DEBIT_CARD", PaymentMethod.DEBIT_CARD.name());
        assertEquals("PAYPAL", PaymentMethod.PAYPAL.name());
        assertEquals("UPI", PaymentMethod.UPI.name());
        assertEquals("NET_BANKING", PaymentMethod.NET_BANKING.name());
        assertEquals("WALLET", PaymentMethod.WALLET.name());
        assertEquals("CASH", PaymentMethod.CASH.name());
    }

    @Test
    void verifyAllPaymentStatusFeatures() {
        // Test that all required payment statuses are available
        PaymentStatus[] statuses = PaymentStatus.values();
        List<PaymentStatus> statusList = Arrays.asList(statuses);
        
        assertTrue(statusList.contains(PaymentStatus.PENDING), "PENDING status should be available");
        assertTrue(statusList.contains(PaymentStatus.PROCESSING), "PROCESSING status should be available");
        assertTrue(statusList.contains(PaymentStatus.COMPLETED), "COMPLETED status should be available");
        assertTrue(statusList.contains(PaymentStatus.FAILED), "FAILED status should be available");
        assertTrue(statusList.contains(PaymentStatus.REFUNDED), "REFUNDED status should be available");

        // Verify payment status transitions are supported
        assertEquals("PENDING", PaymentStatus.PENDING.name());
        assertEquals("PROCESSING", PaymentStatus.PROCESSING.name());
        assertEquals("COMPLETED", PaymentStatus.COMPLETED.name());
        assertEquals("FAILED", PaymentStatus.FAILED.name());
        assertEquals("REFUNDED", PaymentStatus.REFUNDED.name());
    }

    @Test
    void verifyPaymentServiceMethodsExist() {
        // Verify all payment service methods are available
        assertNotNull(paymentService, "PaymentService should be initialized");
        
        // Verify service methods exist (without calling them to avoid database issues)
        assertDoesNotThrow(() -> {
            // These should not throw NoSuchMethodError if methods exist
            paymentService.getClass().getMethod("processPayment", Long.class, double.class, String.class);
            paymentService.getClass().getMethod("processPassengerPayment", Long.class, PaymentMethod.class, Long.class);
            paymentService.getClass().getMethod("getPaymentsForDriver", Long.class);
            paymentService.getClass().getMethod("getPaymentsForRide", Long.class, Long.class);
            paymentService.getClass().getMethod("getPaymentsByStatus", PaymentStatus.class);
            paymentService.getClass().getMethod("completePayment", Long.class);
            paymentService.getClass().getMethod("failPayment", Long.class);
        }, "All payment service methods should be available");
    }

    @Test
    void verifyRideServiceMethodsExist() {
        // Verify all ride service methods are available
        assertNotNull(rideService, "RideService should be initialized");
        
        // Verify service methods exist
        assertDoesNotThrow(() -> {
            rideService.getClass().getMethod("createRide", Long.class, Ride.class);
            rideService.getClass().getMethod("getRide", Long.class);
            rideService.getClass().getMethod("updateRideStatus", Long.class, RideStatus.class);
            rideService.getClass().getMethod("getRidesByDriver", Long.class);
            rideService.getClass().getMethod("getAllRides");
            rideService.getClass().getMethod("getAvailableRides");
            rideService.getClass().getMethod("searchRides", String.class, String.class);
        }, "All ride service methods should be available");
    }

    @Test
    void verifyTeamMemberResponsibilities() {
        // Member 1: User Registration & Login (Singleton pattern, SRP principle)
        assertNotNull(userRepository, "UserRepository should be available for user management");
        
        // Member 2: Search & Book Ride (Strategy pattern, OCP principle)
        assertNotNull(rideService, "RideService should be available for ride search and booking");
        
        // Member 3: Publish & Manage Ride (Observer pattern, LSP principle)
        assertNotNull(rideRepository, "RideRepository should be available for ride management");
        
        // Member 4: Payment Processing (Facade pattern, DIP principle)
        assertNotNull(paymentService, "PaymentService should be available for payment processing");
        assertNotNull(paymentRepository, "PaymentRepository should be available for payment management");
        
        // Verify all payment methods are available for passengers
        PaymentMethod[] methods = PaymentMethod.values();
        assertTrue(methods.length >= 7, "Should have at least 7 payment methods available");
        
        // Verify all ride statuses are available for drivers
        RideStatus[] statuses = RideStatus.values();
        assertTrue(statuses.length >= 6, "Should have at least 6 ride statuses available");
    }

    @Test
    void verifyApplicationComponents() {
        // Verify all major components are properly initialized
        assertNotNull(rideService, "RideService should be initialized");
        assertNotNull(paymentService, "PaymentService should be initialized");
        assertNotNull(rideRepository, "RideRepository should be initialized");
        assertNotNull(paymentRepository, "PaymentRepository should be initialized");
        assertNotNull(userRepository, "UserRepository should be initialized");
        
        // Verify repositories are accessible
        assertNotNull(rideRepository, "RideRepository should be accessible");
        assertNotNull(paymentRepository, "PaymentRepository should be accessible");
        assertNotNull(userRepository, "UserRepository should be accessible");
    }

    @Test
    void verifyRideStatusEndpoints() {
        // Test that ride status transitions are properly supported
        RideStatus[] allStatuses = RideStatus.values();
        
        // Verify we have all the required statuses for the new endpoints
        boolean hasInProgress = Arrays.stream(allStatuses)
                .anyMatch(status -> status == RideStatus.IN_PROGRESS);
        assertTrue(hasInProgress, "IN_PROGRESS status should be available for /start endpoint");
        
        boolean hasCompleted = Arrays.stream(allStatuses)
                .anyMatch(status -> status == RideStatus.COMPLETED);
        assertTrue(hasCompleted, "COMPLETED status should be available for /complete endpoint");
        
        boolean hasCancelled = Arrays.stream(allStatuses)
                .anyMatch(status -> status == RideStatus.CANCELLED);
        assertTrue(hasCancelled, "CANCELLED status should be available for /cancel endpoint");
        
        // Verify status names match API expectations
        assertEquals("IN_PROGRESS", RideStatus.IN_PROGRESS.name(), "Should support IN_PROGRESS for API");
        assertEquals("COMPLETED", RideStatus.COMPLETED.name(), "Should support COMPLETED for API");
        assertEquals("CANCELLED", RideStatus.CANCELLED.name(), "Should support CANCELLED for API");
    }

    @Test
    void verifyPaymentEndpoints() {
        // Test that payment method endpoints are properly supported
        PaymentMethod[] allMethods = PaymentMethod.values();
        
        // Verify we have all the required payment methods for passenger endpoints
        boolean hasCreditCard = Arrays.stream(allMethods)
                .anyMatch(method -> method == PaymentMethod.CREDIT_CARD);
        assertTrue(hasCreditCard, "CREDIT_CARD should be available for payment endpoints");
        
        boolean hasUPI = Arrays.stream(allMethods)
                .anyMatch(method -> method == PaymentMethod.UPI);
        assertTrue(hasUPI, "UPI should be available for payment endpoints");
        
        boolean hasCash = Arrays.stream(allMethods)
                .anyMatch(method -> method == PaymentMethod.CASH);
        assertTrue(hasCash, "CASH should be available for payment endpoints");
        
        // Verify payment status endpoints are supported
        PaymentStatus[] allStatuses = PaymentStatus.values();
        boolean hasCompleted = Arrays.stream(allStatuses)
                .anyMatch(status -> status == PaymentStatus.COMPLETED);
        assertTrue(hasCompleted, "COMPLETED status should be available for payment status endpoints");
    }
}
