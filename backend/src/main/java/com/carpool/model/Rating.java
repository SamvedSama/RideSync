package com.carpool.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
public class Rating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_user_id", nullable = false)
    private User ratedUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rating_user_id", nullable = false)
    private User ratingUser;
    
    @Column(nullable = false)
    private int rating; // 1-5 stars
    
    private String comment;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public Rating() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Rating(Booking booking, User ratedUser, User ratingUser, int rating, String comment) {
        this();
        this.booking = booking;
        this.ratedUser = ratedUser;
        this.ratingUser = ratingUser;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    
    public User getRatedUser() { return ratedUser; }
    public void setRatedUser(User ratedUser) { this.ratedUser = ratedUser; }
    
    public User getRatingUser() { return ratingUser; }
    public void setRatingUser(User ratingUser) { this.ratingUser = ratingUser; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
