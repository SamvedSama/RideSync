package com.carpool.dto;

import com.carpool.model.UserRole;

public class AuthResponse {

    private String token;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private boolean banned;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String name, String email,
                         String phone, UserRole role, boolean banned) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.banned = banned;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }
}
