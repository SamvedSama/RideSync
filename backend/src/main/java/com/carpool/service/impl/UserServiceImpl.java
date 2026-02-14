package com.carpool.service.impl;

import com.carpool.model.User;
import com.carpool.repository.UserRepository;
import com.carpool.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(User user) {

        repository.findByEmail(user.getEmail())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Email already registered");
                });

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return repository.save(user);
    }

    @Override
    public User login(String email, String password) {

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user;
    }

    @Override
    public User updateProfile(Long userId, String name, String phone) {

        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(name);
        user.setPhone(phone);

        return repository.save(user);
    }
}
