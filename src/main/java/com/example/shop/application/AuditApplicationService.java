package com.example.shop.application;

import com.example.shop.domain.AuditPort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditApplicationService {

    private final AuditPort auditPort;

    public AuditApplicationService(AuditPort auditPort) {
        this.auditPort = auditPort;
    }

    public List<String> getAuditLogs() {
        return auditPort.getLogs();
    }
}