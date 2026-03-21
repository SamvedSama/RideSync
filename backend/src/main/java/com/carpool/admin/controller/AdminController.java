package com.carpool.admin.controller;

import com.carpool.admin.model.AdminAuditLog;
import com.carpool.admin.service.AdminService;
import com.carpool.model.Ride;
import com.carpool.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<User> banUser(@PathVariable Long userId,
                                         @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(adminService.banUser(admin.getUserId(), userId));
    }

    @PutMapping("/users/{userId}/unban")
    public ResponseEntity<User> unbanUser(@PathVariable Long userId,
                                           @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(adminService.unbanUser(admin.getUserId(), userId));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> changeRole(@PathVariable Long userId,
                                            @RequestParam String role,
                                            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(adminService.changeUserRole(admin.getUserId(), userId, role));
    }

    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(adminService.getAllRides());
    }

    @PutMapping("/rides/{rideId}/cancel")
    public ResponseEntity<Ride> cancelRide(@PathVariable Long rideId,
                                            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(adminService.adminCancelRide(admin.getUserId(), rideId));
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AdminAuditLog>> getAuditLog() {
        return ResponseEntity.ok(adminService.getAuditLog());
    }

    @GetMapping("/audit/by-admin")
    public ResponseEntity<List<AdminAuditLog>> getAuditByAdmin(@AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(adminService.getAuditLogByAdmin(admin.getUserId()));
    }
}
