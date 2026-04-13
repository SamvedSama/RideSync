package com.carpool.controller;

import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.service.RideService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testDriver;
    private Ride testRide;

    @BeforeEach
    void setUp() {
        testDriver = new User();
        testDriver.setUserId(1L);
        testDriver.setEmail("driver@test.com");
        testDriver.setRole(UserRole.DRIVER);

        testRide = new Ride();
        testRide.setId(1L);
        testRide.setSource("Source");
        testRide.setDestination("Destination");
        testRide.setTotalSeats(4);
        testRide.setFarePerSeat(100.0);
        testRide.setDepartureTime(LocalDateTime.now().plusHours(1));
        testRide.setStatus(RideStatus.PUBLISHED);
        testRide.setDriver(testDriver);
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testCreateRide() throws Exception {
        when(rideService.createRide(any(), any())).thenReturn(testRide);

        mockMvc.perform(post("/api/rides")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRide)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("Source"))
                .andExpect(jsonPath("$.destination").value("Destination"));
    }

    @Test
    void testSearchRides() throws Exception {
        List<Ride> rides = Arrays.asList(testRide);
        when(rideService.getAvailableRides()).thenReturn(rides);

        mockMvc.perform(get("/api/rides/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source").value("Source"));
    }

    @Test
    void testGetRide() throws Exception {
        when(rideService.getRide(1L)).thenReturn(testRide);

        mockMvc.perform(get("/api/rides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testGetMyRides() throws Exception {
        List<Ride> rides = Arrays.asList(testRide);
        when(rideService.getRidesByDriver(1L)).thenReturn(rides);

        mockMvc.perform(get("/api/rides/my-rides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driver.userId").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllRides() throws Exception {
        List<Ride> rides = Arrays.asList(testRide);
        when(rideService.getAllRides()).thenReturn(rides);

        mockMvc.perform(get("/api/rides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testUpdateRideStatus() throws Exception {
        testRide.setStatus(RideStatus.IN_PROGRESS);
        when(rideService.updateRideStatus(1L, RideStatus.IN_PROGRESS)).thenReturn(testRide);

        mockMvc.perform(put("/api/rides/1/status")
                .with(csrf())
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testStartRide() throws Exception {
        testRide.setStatus(RideStatus.IN_PROGRESS);
        when(rideService.updateRideStatus(1L, RideStatus.IN_PROGRESS)).thenReturn(testRide);

        mockMvc.perform(put("/api/rides/1/start")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testCompleteRide() throws Exception {
        testRide.setStatus(RideStatus.COMPLETED);
        when(rideService.updateRideStatus(1L, RideStatus.COMPLETED)).thenReturn(testRide);

        mockMvc.perform(put("/api/rides/1/complete")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    void testCancelRide() throws Exception {
        testRide.setStatus(RideStatus.CANCELLED);
        when(rideService.updateRideStatus(1L, RideStatus.CANCELLED)).thenReturn(testRide);

        mockMvc.perform(put("/api/rides/1/cancel")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = {"PASSENGER"})
    void testStartRideUnauthorized() throws Exception {
        mockMvc.perform(put("/api/rides/1/start")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCancelRideAsAdmin() throws Exception {
        testRide.setStatus(RideStatus.CANCELLED);
        when(rideService.updateRideStatus(1L, RideStatus.CANCELLED)).thenReturn(testRide);

        mockMvc.perform(put("/api/rides/1/cancel")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
