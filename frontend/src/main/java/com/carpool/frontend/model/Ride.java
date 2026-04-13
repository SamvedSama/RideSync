package com.carpool.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Ride {
    private Long id;
    private String source;
    private String destination;
    private int totalSeats;
    private double farePerSeat;
    private LocalDateTime departureTime;
    private RideStatus status;
    private DriverInfo driver;
    private Integer availableSeats;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DriverInfo {
        private String name;
        private String email;
        private String phone;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public double getFarePerSeat() { return farePerSeat; }
    public void setFarePerSeat(double farePerSeat) { this.farePerSeat = farePerSeat; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }

    public DriverInfo getDriver() { return driver; }
    public void setDriver(DriverInfo driver) { this.driver = driver; }

    public Integer getAvailableSeats() { 
        if (availableSeats != null) {
            return availableSeats;
        }
        // Calculate available seats if not provided by backend
        if (totalSeats > 0) {
            return totalSeats; // Default to total seats if no bookings data available
        }
        return 0; // Default fallback
    }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
}
