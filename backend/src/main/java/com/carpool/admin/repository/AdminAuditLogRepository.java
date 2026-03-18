package com.carpool.admin.repository;

import com.carpool.admin.model.AdminAuditLog;
import com.carpool.admin.model.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    List<AdminAuditLog> findByAdminId(Long adminId);

    List<AdminAuditLog> findByAction(AdminAction action);

    List<AdminAuditLog> findByTargetTypeAndTargetId(String targetType, Long targetId);
}
