package com.carpool.controller;

import com.carpool.model.Payment;
import com.carpool.model.PaymentStatus;
import com.carpool.model.PaymentMethod;
import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@test.com");
        testUser.setRole(UserRole.PASSENGER);

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setAmount(100.0);
        testPayment.setPaymentMethod("CREDIT_CARD");
        testPayment.setStatus(PaymentStatus.COMPLETED);
    }

    @Test
    @WithMockUser(roles = {"PASSENGER"})
    void testProcessPayment() throws Exception {
        when(paymentService.processPayment(any(), any(), any())).thenReturn(testPayment);

        mockMvc.perform(post("/api/payments/process")
                .with(csrf())
                .param("bookingId", "1")
                .param("amount", "100.0")
                .param("paymentMethod", "CREDIT_CARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));
    }

    @Test
    @WithMockUser(roles = {"PASSENGER"})
    void testPassengerPayment() throws Exception {
        when(paymentService.processPassengerPayment(any(), any(), any())).thenReturn(testPayment);

        mockMvc.perform(post("/api/payments/passenger/pay")
                .with(csrf())
                .param("bookingId", "1")
                .param("paymentMethod", "CREDIT_CARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void testGetAvailablePaymentMethods() throws Exception {
        mockMvc.perform(get("/api/payments/methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("CREDIT_CARD"))
                .andExpect(jsonPath("$[1]").value("DEBIT_CARD"))
                .andExpect(jsonPath("$[2]").value("PAYPAL"));
    }

    @Test
    @WithMockUser(roles = {"PASSENGER"})
    void testGetPaymentByBooking() throws Exception {
        when(paymentService.getPaymentByBookingId(1L)).thenReturn(Optional.of(testPayment));

        mockMvc.perform(get("/api/payments/booking/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    @WithMockUser(roles = {"PASSENGER"})
    void testGetPaymentByBookingNotFound() throws Exception {
        when(paymentService.getPaymentByBookingId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/booking/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testCompletePayment() throws Exception {
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.completePayment(1L)).thenReturn(testPayment);

        mockMvc.perform(post("/api/payments/1/complete")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testFailPayment() throws Exception {
        testPayment.setStatus(PaymentStatus.FAILED);
        when(paymentService.failPayment(1L)).thenReturn(testPayment);

        mockMvc.perform(post("/api/payments/1/fail")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetPaymentsByStatus() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/status/COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testGetDriverPayments() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsForDriver(1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/driver/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").doesNotExist());
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testGetRidePayments() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsForRide(1L, 1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/driver/ride/1/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }

    @Test
    @WithMockUser(roles = {"PASSENGER"})
    void testDriverPaymentsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/payments/driver/payments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"PASSENGER"})
    void testCompletePaymentUnauthorized() throws Exception {
        mockMvc.perform(post("/api/payments/1/complete")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetPaymentsByStatusUnauthorized() throws Exception {
        mockMvc.perform(get("/api/payments/status/COMPLETED"))
                .andExpect(status().isOk());
    }
}
