package com.carpool.admin.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AdminAuditLog — persisted record of every action an admin performs.
 * Supports Admin Management (Member 4 minor use case).
 */
@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminAction action;

    @Column(nullable = false)
    private String targetType;   // "USER", "RIDE", "PAYMENT"

    @Column(nullable = false)
    private Long targetId;

    private String notes;

    @Column(nullable = false)
    private LocalDateTime performedAt;

    public AdminAuditLog() {}

    public AdminAuditLog(Long adminId, AdminAction action,
                          String targetType, Long targetId, String notes) {
        this.adminId = adminId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.notes = notes;
        this.performedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public AdminAction getAction() { return action; }
    public void setAction(AdminAction action) { this.action = action; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
}
