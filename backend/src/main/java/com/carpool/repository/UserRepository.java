package com.carpool.repository;

import com.carpool.model.User;
import com.carpool.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
    // Additional methods needed by services
    long countByRole(UserRole role);
}
