package com.carpool.admin.controller;

import com.carpool.admin.model.AdminAuditLog;
import com.carpool.admin.service.AdminService;
import com.carpool.model.Ride;
import com.carpool.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminController — REST API for Admin Management (Member 4 minor use case).
 *
 * All endpoints are prefixed with /api/admin.
 * Every mutating action requires an adminId for audit logging.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ── User Management ──────────────────────────────────────────────────────

    /**
     * GET /api/admin/users
     * List all registered users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * PUT /api/admin/users/{userId}/ban?adminId=1
     * Ban a user.
     */
    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<User> banUser(@PathVariable Long userId,
                                         @RequestParam Long adminId) {
        return ResponseEntity.ok(adminService.banUser(adminId, userId));
    }

    /**
     * PUT /api/admin/users/{userId}/unban?adminId=1
     * Unban a user.
     */
    @PutMapping("/users/{userId}/unban")
    public ResponseEntity<User> unbanUser(@PathVariable Long userId,
                                           @RequestParam Long adminId) {
        return ResponseEntity.ok(adminService.unbanUser(adminId, userId));
    }

    /**
     * PUT /api/admin/users/{userId}/role?adminId=1&role=DRIVER
     * Change a user's role.
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> changeRole(@PathVariable Long userId,
                                            @RequestParam Long adminId,
                                            @RequestParam String role) {
        return ResponseEntity.ok(adminService.changeUserRole(adminId, userId, role));
    }

    // ── Ride Management ──────────────────────────────────────────────────────

    /**
     * GET /api/admin/rides
     * List all rides in the system.
     */
    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(adminService.getAllRides());
    }

    /**
     * PUT /api/admin/rides/{rideId}/cancel?adminId=1
     * Force-cancel any ride.
     */
    @PutMapping("/rides/{rideId}/cancel")
    public ResponseEntity<Ride> cancelRide(@PathVariable Long rideId,
                                            @RequestParam Long adminId) {
        return ResponseEntity.ok(adminService.adminCancelRide(adminId, rideId));
    }

    // ── Audit Log ────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/audit
     * Full audit log of all admin actions.
     */
    @GetMapping("/audit")
    public ResponseEntity<List<AdminAuditLog>> getAuditLog() {
        return ResponseEntity.ok(adminService.getAuditLog());
    }

    /**
     * GET /api/admin/audit?adminId=1
     * Audit log filtered by admin.
     */
    @GetMapping("/audit/by-admin")
    public ResponseEntity<List<AdminAuditLog>> getAuditByAdmin(@RequestParam Long adminId) {
        return ResponseEntity.ok(adminService.getAuditLogByAdmin(adminId));
    }
}
