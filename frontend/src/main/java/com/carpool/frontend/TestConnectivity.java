package com.carpool.frontend;

import com.carpool.frontend.model.AuthResponse;
import com.carpool.frontend.service.AuthService;

public class TestConnectivity {
    public static void main(String[] args) {
        AuthService authService = new AuthService();
        try {
            System.out.println("Testing Backend Connectivity...");
            // Create user
            String email = "test" + System.currentTimeMillis() + "@test.com";
            AuthResponse regRsp = authService.register("Test User", email, "password123", "1234567890", "RIDER");
            System.out.println("Registration Successful! Token: " + regRsp.getToken());

            // Login user
            AuthResponse logRsp = authService.login(email, "password123");
            System.out.println("Login Successful! Name: " + logRsp.getName());
            
            System.out.println("ALL TESTS PASSED.");
        } catch (Exception e) {
            System.err.println("TEST FAILED.");
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
