package com.carpool.service;

import com.carpool.model.User;

public interface UserService {

    User register(User user);

    User login(String email, String password);

    User updateProfile(Long userId, String name, String phone);
}
