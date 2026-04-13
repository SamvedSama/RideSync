package com.carpool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

    @NotBlank(message = "Source cannot be blank")
    @Column(nullable = false)
    private String source;

    @NotBlank(message = "Destination cannot be blank")
    @Column(nullable = false)
    private String destination;

    @Min(value = 1, message = "Total seats must be at least 1")
    @Column(nullable = false)
    private int totalSeats;

    @Min(value = 0, message = "Fare cannot be negative")
    @Column(nullable = false)
    private double farePerSeat;

    @NotNull(message = "Departure time is required")
    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    @ManyToOne(optional = false)
    @JsonIgnore
    private User driver;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    // Transient field — populated server-side before serialization,
    // READ_ONLY so Jackson never tries to deserialize it back from JSON.
    @Transient
    @JsonProperty("availableSeats")
    private Integer availableSeats;

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

    /**
     * Computes available seats from the loaded bookings collection.
     * Must be called while the Hibernate session is open (i.e. inside a
     * 
     * @Transactional boundary) so the bookings list is populated.
     *                The result is stored in the transient field and serialized as
     *                JSON.
     */
    @JsonIgnore
    public int getAvailableSeats() {
        if (availableSeats != null)
            return availableSeats;
        if (bookings == null || bookings.isEmpty())
            return totalSeats;
        int booked = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || 
                             b.getStatus() == BookingStatus.OTP_PENDING || 
                             b.getStatus() == BookingStatus.IN_PROGRESS)
                .mapToInt(Booking::getSeatsBooked)
                .sum();
        return totalSeats - booked;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setRide(this);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setRide(null);
    }

        @JsonIgnore
    public boolean hasActiveBookings() {
        return bookings != null && bookings.stream()
                .anyMatch(b -> b.getStatus() != BookingStatus.CANCELLED);
    }

    @JsonIgnore
    public boolean hasOtpPendingBooking() {
        return bookings != null && bookings.stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.OTP_PENDING);
    }

    @JsonIgnore
    public boolean hasInProgressBooking() {
        return bookings != null && bookings.stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.IN_PROGRESS);
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
}
