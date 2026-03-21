package com.carpool.dto;

import com.carpool.model.Booking;
import com.carpool.model.BookingStatus;

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
    private double totalFare;

    public static BookingResponse from(Booking b) {
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
        r.totalFare = b.getRide().getFarePerSeat() * b.getSeatsBooked();
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
    public double getTotalFare() { return totalFare; }
}
