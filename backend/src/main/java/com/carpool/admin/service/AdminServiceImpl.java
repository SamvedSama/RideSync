package com.carpool.admin.service;

import com.carpool.admin.model.AdminAction;
import com.carpool.admin.model.AdminAuditLog;
import com.carpool.admin.repository.AdminAuditLogRepository;
import com.carpool.model.Ride;
import com.carpool.model.RideStatus;
import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.repository.RideRepository;
import com.carpool.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AdminServiceImpl — concrete implementation of AdminService.
 *
 * Defence-in-depth: even though the controller guards access via @PreAuthorize,
 * the service still validates the admin role so it cannot be misused if called
 * programmatically from another service.
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final AdminAuditLogRepository auditLogRepository;

    public AdminServiceImpl(UserRepository userRepository,
                             RideRepository rideRepository,
                             AdminAuditLogRepository auditLogRepository) {
        this.userRepository  = userRepository;
        this.rideRepository  = rideRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User banUser(Long adminId, Long targetUserId) {
        assertAdmin(adminId);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUserId));

        if (target.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Cannot ban another admin");
        }

        target.setBanned(true);
        User saved = userRepository.save(target);
        log(adminId, AdminAction.USER_BANNED, "USER", targetUserId, "User banned by admin " + adminId);
        return saved;
    }

    @Override
    public User unbanUser(Long adminId, Long targetUserId) {
        assertAdmin(adminId);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUserId));

        target.setBanned(false);
        User saved = userRepository.save(target);
        log(adminId, AdminAction.USER_UNBANNED, "USER", targetUserId, "User unbanned by admin " + adminId);
        return saved;
    }

    @Override
    public User changeUserRole(Long adminId, Long targetUserId, String newRole) {
        assertAdmin(adminId);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUserId));

        UserRole role;
        try {
            role = UserRole.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newRole + ". Must be RIDER, DRIVER, or ADMIN");
        }

        target.setRole(role);
        User saved = userRepository.save(target);
        log(adminId, AdminAction.USER_ROLE_CHANGED, "USER", targetUserId,
                "Role changed to " + newRole + " by admin " + adminId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @Override
    public Ride adminCancelRide(Long adminId, Long rideId) {
        assertAdmin(adminId);

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + rideId));

        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed ride");
        }
        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalArgumentException("Ride is already cancelled");
        }

        ride.setStatus(RideStatus.CANCELLED);
        Ride saved = rideRepository.save(ride);
        log(adminId, AdminAction.RIDE_CANCELLED, "RIDE", rideId, "Ride cancelled by admin " + adminId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAuditLog> getAuditLog() {
        return auditLogRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAuditLog> getAuditLogByAdmin(Long adminId) {
        return auditLogRepository.findByAdminId(adminId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void assertAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Access denied: only admins can perform this action");
        }
    }

    private void log(Long adminId, AdminAction action,
                     String targetType, Long targetId, String notes) {
        auditLogRepository.save(new AdminAuditLog(adminId, action, targetType, targetId, notes));
    }
}
