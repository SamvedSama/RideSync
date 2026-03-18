package com.carpool.payment.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * MockPaymentGateway simulates external payment processing.
 *
 * In a real system this would call Razorpay / Stripe / PayU.
 * The Facade isolates callers from needing to know about this class.
 */
@Component
public class MockPaymentGateway {

    /**
     * Simulates charging the user.
     * Returns a transaction reference if successful, throws on failure.
     *
     * Simulation rule: amounts ending in .99 are treated as failures (for testing).
     */
    public String charge(double amount, String method) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid payment amount: " + amount);
        }

        // Simulate a deterministic failure for testing
        if (String.valueOf(amount).endsWith(".99")) {
            throw new RuntimeException("Payment gateway declined the transaction");
        }

        // Generate a mock transaction reference
        return "TXN-" + method.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Simulates a refund on a previously successful transaction.
     */
    public boolean refund(String transactionRef) {
        if (transactionRef == null || transactionRef.isBlank()) {
            throw new IllegalArgumentException("Transaction reference is required for refund");
        }
        // Simulate: refunds always succeed in mock
        System.out.println("[MockGateway] Refund issued for transaction: " + transactionRef);
        return true;
    }
}
