package com.carpool.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String preferredLanguage = "English";
    
    @Column(nullable = false)
    private String preferredCurrency = "USD";
    
    @Column(nullable = false)
    private boolean notificationEmail = true;
    
    @Column(nullable = false)
    private boolean notificationSms = false;
    
    @Column(nullable = false)
    private boolean notificationPush = true;
    
    @Column(nullable = false)
    private boolean allowLocationSharing = true;
    
    @Column(nullable = false)
    private boolean allowProfileView = true;
    
    @Column(nullable = false)
    private String preferredSeatType = "Regular";
    
    @Column(nullable = false)
    private boolean allowMusic = true;
    
    @Column(nullable = false)
    private boolean allowSmoking = false;
    
    @Column(nullable = false)
    private boolean allowPets = false;
    
    @Column(nullable = false)
    private boolean allowConversation = true;
    
    @Column(nullable = false)
    private double maxFareRange = 50.0;
    
    @Column(nullable = false)
    private String preferredPaymentMethod = "Credit Card";
    
    @Column(nullable = false)
    private boolean autoPay = false;
    
    @ElementCollection
    @CollectionTable(name = "favorite_routes", joinColumns = @JoinColumn(name = "user_preferences_id"))
    @MapKeyColumn(name = "route_key")
    @Column(name = "route_value")
    private Map<String, String> favoriteRoutes = new HashMap<>();
    
    public UserPreferences() {}
    
    public UserPreferences(User user) {
        this.user = user;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    
    public String getPreferredCurrency() { return preferredCurrency; }
    public void setPreferredCurrency(String preferredCurrency) { this.preferredCurrency = preferredCurrency; }
    
    public boolean isNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(boolean notificationEmail) { this.notificationEmail = notificationEmail; }
    
    public boolean isNotificationSms() { return notificationSms; }
    public void setNotificationSms(boolean notificationSms) { this.notificationSms = notificationSms; }
    
    public boolean isNotificationPush() { return notificationPush; }
    public void setNotificationPush(boolean notificationPush) { this.notificationPush = notificationPush; }
    
    public boolean isAllowLocationSharing() { return allowLocationSharing; }
    public void setAllowLocationSharing(boolean allowLocationSharing) { this.allowLocationSharing = allowLocationSharing; }
    
    public boolean isAllowProfileView() { return allowProfileView; }
    public void setAllowProfileView(boolean allowProfileView) { this.allowProfileView = allowProfileView; }
    
    public String getPreferredSeatType() { return preferredSeatType; }
    public void setPreferredSeatType(String preferredSeatType) { this.preferredSeatType = preferredSeatType; }
    
    public boolean isAllowMusic() { return allowMusic; }
    public void setAllowMusic(boolean allowMusic) { this.allowMusic = allowMusic; }
    
    public boolean isAllowSmoking() { return allowSmoking; }
    public void setAllowSmoking(boolean allowSmoking) { this.allowSmoking = allowSmoking; }
    
    public boolean isAllowPets() { return allowPets; }
    public void setAllowPets(boolean allowPets) { this.allowPets = allowPets; }
    
    public boolean isAllowConversation() { return allowConversation; }
    public void setAllowConversation(boolean allowConversation) { this.allowConversation = allowConversation; }
    
    public double getMaxFareRange() { return maxFareRange; }
    public void setMaxFareRange(double maxFareRange) { this.maxFareRange = maxFareRange; }
    
    public String getPreferredPaymentMethod() { return preferredPaymentMethod; }
    public void setPreferredPaymentMethod(String preferredPaymentMethod) { this.preferredPaymentMethod = preferredPaymentMethod; }
    
    public boolean isAutoPay() { return autoPay; }
    public void setAutoPay(boolean autoPay) { this.autoPay = autoPay; }
    
    public Map<String, String> getFavoriteRoutes() { return favoriteRoutes; }
    public void setFavoriteRoutes(Map<String, String> favoriteRoutes) { this.favoriteRoutes = favoriteRoutes; }
}
