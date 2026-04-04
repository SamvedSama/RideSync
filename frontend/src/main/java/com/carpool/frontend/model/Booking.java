package com.carpool.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {
    private Long id;
    private int seatsBooked;
    private BookingStatus status;
    private Long riderId;
    private String riderName;
    private Long rideId;
    private String source;
    private String destination;
    private double farePerSeat;
    private double waitingCharge;
    private double totalFare;
    private String startOtp;
    private LocalDateTime otpGeneratedAt;
    private LocalDateTime waitingStartedAt;
    private RideStatus rideStatus;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public int getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(int seatsBooked) { this.seatsBooked = seatsBooked; }
    
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    
    public Long getRiderId() { return riderId; }
    public void setRiderId(Long riderId) { this.riderId = riderId; }
    
    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }
    
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public double getFarePerSeat() { return farePerSeat; }
    public void setFarePerSeat(double farePerSeat) { this.farePerSeat = farePerSeat; }
    
    public double getWaitingCharge() { return waitingCharge; }
    public void setWaitingCharge(double waitingCharge) { this.waitingCharge = waitingCharge; }
    
    public double getTotalFare() { return totalFare; }
    public void setTotalFare(double totalFare) { this.totalFare = totalFare; }
    
    public String getStartOtp() { return startOtp; }
    public void setStartOtp(String startOtp) { this.startOtp = startOtp; }
    
    public LocalDateTime getOtpGeneratedAt() { return otpGeneratedAt; }
    public void setOtpGeneratedAt(LocalDateTime otpGeneratedAt) { this.otpGeneratedAt = otpGeneratedAt; }
    
    public LocalDateTime getWaitingStartedAt() { return waitingStartedAt; }
    public void setWaitingStartedAt(LocalDateTime waitingStartedAt) { this.waitingStartedAt = waitingStartedAt; }
    
    public RideStatus getRideStatus() { return rideStatus; }
    public void setRideStatus(RideStatus rideStatus) { this.rideStatus = rideStatus; }
}
