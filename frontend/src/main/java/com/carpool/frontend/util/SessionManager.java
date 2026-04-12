package com.carpool.frontend.util;

import com.carpool.frontend.model.AuthResponse;

public class SessionManager {
    private static AuthResponse currentUser;
    private static Long selectedRideId;

    public static void setCurrentUser(AuthResponse user) {
        currentUser = user;
    }

    public static AuthResponse getCurrentUser() {
        return currentUser;
    }

    public static String getToken() {
        return currentUser != null ? currentUser.getToken() : null;
    }

    public static void clear() {
        currentUser = null;
        selectedRideId = null;
    }
    
    public static void setSelectedRideId(Long rideId) {
        selectedRideId = rideId;
    }
    
    public static Long getSelectedRideId() {
        return selectedRideId;
    }
    
    public static void clearSelectedRideId() {
        selectedRideId = null;
    }
}
