package com.example.shop.application;

import com.example.shop.domain.AuditLog;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditService {
    private final AuditLog auditLog;

    public AuditService(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public List<String> getAuditLogs() {
        return auditLog.getLogs();
    }
}
