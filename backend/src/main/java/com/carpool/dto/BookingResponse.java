package com.carpool.dto;

import com.carpool.model.Booking;
import com.carpool.model.BookingStatus;
import com.carpool.model.RideStatus;

import java.time.LocalDateTime;

public class BookingResponse {
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

    public static BookingResponse from(Booking b) {
        b.refreshWaitingCharge();

        BookingResponse r = new BookingResponse();
        r.id = b.getId();
        r.seatsBooked = b.getSeatsBooked();
        r.status = b.getStatus();
        r.riderId = b.getRider().getUserId();
        r.riderName = b.getRider().getName();
        r.rideId = b.getRide().getId();
        r.source = b.getRide().getSource();
        r.destination = b.getRide().getDestination();
        r.farePerSeat = b.getRide().getFarePerSeat();
        r.waitingCharge = b.getWaitingCharge();
        r.totalFare = b.getTotalFare();
        r.startOtp = b.getStartOtp();
        r.otpGeneratedAt = b.getOtpGeneratedAt();
        r.waitingStartedAt = b.getWaitingStartedAt();
        r.rideStatus = b.getRide().getStatus();
        return r;
    }

    public Long getId() { return id; }
    public int getSeatsBooked() { return seatsBooked; }
    public BookingStatus getStatus() { return status; }
    public Long getRiderId() { return riderId; }
    public String getRiderName() { return riderName; }
    public Long getRideId() { return rideId; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public double getFarePerSeat() { return farePerSeat; }
    public double getWaitingCharge() { return waitingCharge; }
    public double getTotalFare() { return totalFare; }
    public String getStartOtp() { return startOtp; }
    public LocalDateTime getOtpGeneratedAt() { return otpGeneratedAt; }
    public LocalDateTime getWaitingStartedAt() { return waitingStartedAt; }
    public RideStatus getRideStatus() { return rideStatus; }
}