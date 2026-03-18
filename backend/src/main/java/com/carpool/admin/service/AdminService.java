package com.carpool.admin.service;

import com.carpool.admin.model.AdminAuditLog;
import com.carpool.model.Ride;
import com.carpool.model.User;

import java.util.List;

/**
 * AdminService interface.
 *
 * Design Principle: Dependency Inversion Principle (DIP)
 * AdminController depends on this abstraction, not the concrete AdminServiceImpl.
 */
public interface AdminService {

    // ── User Management ──────────────────────────────────────────
    List<User> getAllUsers();

    User banUser(Long adminId, Long targetUserId);

    User unbanUser(Long adminId, Long targetUserId);

    User changeUserRole(Long adminId, Long targetUserId, String newRole);

    // ── Ride Management ──────────────────────────────────────────
    List<Ride> getAllRides();

    Ride adminCancelRide(Long adminId, Long rideId);

    // ── Audit Log ────────────────────────────────────────────────
    List<AdminAuditLog> getAuditLog();

    List<AdminAuditLog> getAuditLogByAdmin(Long adminId);
}
