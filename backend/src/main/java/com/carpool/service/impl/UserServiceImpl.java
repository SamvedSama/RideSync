package com.carpool.service.impl;

import com.carpool.dto.AuthResponse;
import com.carpool.model.User;
import com.carpool.repository.UserRepository;
import com.carpool.security.JwtTokenProvider;
import com.carpool.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(UserRepository repository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public AuthResponse register(User user) {
        repository.findByEmail(user.getEmail())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Email already registered");
                });

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = repository.save(user);

        String token = jwtTokenProvider.generateToken(
                saved.getUserId(), saved.getEmail(), saved.getRole().name());

        return toAuthResponse(token, saved);
    }

    @Override
    public AuthResponse login(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (user.isBanned()) {
            throw new IllegalArgumentException("Your account has been banned. Please contact support.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(
                user.getUserId(), user.getEmail(), user.getRole().name());

        return toAuthResponse(token, user);
    }

    @Override
    public User updateProfile(Long userId, String name, String phone) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setName(name);
        user.setPhone(phone);
        return repository.save(user);
    }

    @Override
    public User getProfile(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private AuthResponse toAuthResponse(String token, User user) {
        return new AuthResponse(token, user.getUserId(), user.getName(),
                user.getEmail(), user.getPhone(), user.getRole(), user.isBanned());
    }
}
