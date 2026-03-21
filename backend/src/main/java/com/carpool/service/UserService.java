package com.carpool.service;

import com.carpool.dto.AuthResponse;
import com.carpool.model.User;

public interface UserService {
    AuthResponse register(User user);
    AuthResponse login(String email, String password);
    User updateProfile(Long userId, String name, String phone);
    User getProfile(Long userId);
}
