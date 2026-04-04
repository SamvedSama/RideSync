package com.carpool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    private static final int OTP_GRACE_MINUTES = 2;
    private static final double WAITING_CHARGE_PER_BLOCK = 5.0;
    private static final int WAITING_BLOCK_MINUTES = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int seatsBooked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(length = 6)
    private String startOtp;

    private LocalDateTime otpGeneratedAt;

    private LocalDateTime waitingStartedAt;

    @Column(nullable = false)
    private double waitingCharge = 0.0;

    @ManyToOne(optional = false)
    @JsonIgnore
    private User rider;

    @ManyToOne(optional = false)
    @JsonIgnore
    private Ride ride;

    public Booking() {}

    public Booking(User rider, Ride ride, int seatsBooked) {
        this.rider = rider;
        this.ride = ride;
        this.seatsBooked = seatsBooked;
        this.status = BookingStatus.OTP_PENDING;
    }

    public void generateOtp(String otp) {
        this.startOtp = otp;
        this.otpGeneratedAt = LocalDateTime.now();
        this.waitingStartedAt = null;
        this.waitingCharge = 0.0;
        this.status = BookingStatus.OTP_PENDING;
    }

    public void refreshWaitingCharge() {
        if (status != BookingStatus.OTP_PENDING || otpGeneratedAt == null) {
            return;
        }

        LocalDateTime graceEndsAt = otpGeneratedAt.plusMinutes(OTP_GRACE_MINUTES);

        if (LocalDateTime.now().isAfter(graceEndsAt)) {
            if (waitingStartedAt == null) {
                waitingStartedAt = graceEndsAt;
            }

            long waitingMinutes = java.time.Duration.between(waitingStartedAt, LocalDateTime.now()).toMinutes();
            long blocks = waitingMinutes / WAITING_BLOCK_MINUTES;
            this.waitingCharge = blocks * WAITING_CHARGE_PER_BLOCK;
        }
    }

    public double getTotalFare() {
        double baseFare = ride.getFarePerSeat() * seatsBooked;
        return baseFare + waitingCharge;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(int seatsBooked) { this.seatsBooked = seatsBooked; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getStartOtp() { return startOtp; }
    public void setStartOtp(String startOtp) { this.startOtp = startOtp; }

    public LocalDateTime getOtpGeneratedAt() { return otpGeneratedAt; }
    public void setOtpGeneratedAt(LocalDateTime otpGeneratedAt) { this.otpGeneratedAt = otpGeneratedAt; }

    public LocalDateTime getWaitingStartedAt() { return waitingStartedAt; }
    public void setWaitingStartedAt(LocalDateTime waitingStartedAt) { this.waitingStartedAt = waitingStartedAt; }

    public double getWaitingCharge() { return waitingCharge; }
    public void setWaitingCharge(double waitingCharge) { this.waitingCharge = waitingCharge; }

    public User getRider() { return rider; }
    public void setRider(User rider) { this.rider = rider; }

    public Ride getRide() { return ride; }
    public void setRide(Ride ride) { this.ride = ride; }
}