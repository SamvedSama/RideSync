package com.carpool.frontend.util;

import com.carpool.frontend.model.AuthResponse;

public class SessionManager {
    private static AuthResponse currentUser;

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
    }
}
