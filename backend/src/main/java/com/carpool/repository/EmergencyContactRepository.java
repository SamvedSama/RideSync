package com.carpool.repository;

import com.carpool.model.EmergencyContact;
import com.carpool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserAndIsActiveOrderByCreatedAtDesc(User user, boolean isActive);
    List<EmergencyContact> findByUser(User user);
    void deleteByUser(User user);
}
