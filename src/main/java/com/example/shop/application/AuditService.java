package com.example.shop.application;

import com.example.shop.domain.AuditPort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditService {
    private final AuditPort auditPort;

    public AuditService(AuditPort auditPort) {
        this.auditPort = auditPort;
    }

    public List<String> getAuditLogs() {
        return auditPort.getLogs();
    }
}