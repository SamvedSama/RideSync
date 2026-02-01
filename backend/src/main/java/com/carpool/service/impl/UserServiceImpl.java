package com.carpool.service.impl;

import com.carpool.model.User;
import com.carpool.repository.UserRepository;
import com.carpool.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User register(User user) {
        repository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already registered");
        });
        return repository.save(user);
    }

    @Override
    public User login(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid credentials");
        }
        return user;
    }

    @Override
    public User updateProfile(Long userId, String name, String phone) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(name);
        user.setPhone(phone);
        return repository.save(user);
    }
}
