package com.carpool.service;

import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.repository.UserRepository;
import com.carpool.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User Service for handling user operations
 * Follows Single Responsibility Principle
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String issueToken(User user) {
        return jwtTokenProvider.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name());
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Create new user
     */
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        return userRepository.save(user);
    }
    
    /**
     * Update user
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findAll().stream()
            .filter(user -> user.getRole() == role)
            .toList();
    }
    
    /**
     * Update user rating
     */
    public User updateUserRating(Long userId, double newRating) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRating(newRating);
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("User not found");
    }
    
    /**
     * Verify user
     */
    public User verifyUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setVerified(true);
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("User not found");
    }
    
    /**
     * Ban/unban user
     */
    public User banUser(Long userId, boolean banned) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setBanned(banned);
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("User not found");
    }
    
    /**
     * Register a new user
     */
    public com.carpool.dto.AuthResponse register(User user) {
        User createdUser = createUser(user);
        return new com.carpool.dto.AuthResponse(
            issueToken(createdUser),
            createdUser.getId(),
            createdUser.getName(),
            createdUser.getEmail(),
            createdUser.getPhone(),
            createdUser.getRole(),
            createdUser.isBanned()
        );
    }
    
    /**
     * Login user (validate credentials)
     */
    public com.carpool.dto.AuthResponse login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password) && !user.isBanned()) {
                return new com.carpool.dto.AuthResponse(
                    issueToken(user),
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole(),
                    user.isBanned()
                );
            }
        }
        throw new IllegalArgumentException("Invalid credentials");
    }
    
    /**
     * Update user profile
     */
    public User updateProfile(Long userId, String name, String phone) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(name);
            user.setPhone(phone);
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("User not found");
    }
    
    /**
     * Get user profile
     */
    public User getProfile(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        throw new IllegalArgumentException("User not found");
    }
}
