package com.carpool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private double farePerSeat;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    @ManyToOne(optional = false)
    private User driver;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    public Ride() {
    }

    public Ride(String source, String destination, int totalSeats,
            double farePerSeat, LocalDateTime departureTime, User driver) {
        this.source = source;
        this.destination = destination;
        this.totalSeats = totalSeats;
        this.farePerSeat = farePerSeat;
        this.departureTime = departureTime;
        this.driver = driver;
        this.status = RideStatus.PUBLISHED;
    }

    public int getAvailableSeats() {
        int booked = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .mapToInt(Booking::getSeatsBooked)
                .sum();
        return totalSeats - booked;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setRide(this);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setRide(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public double getFarePerSeat() {
        return farePerSeat;
    }

    public void setFarePerSeat(double farePerSeat) {
        this.farePerSeat = farePerSeat;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
