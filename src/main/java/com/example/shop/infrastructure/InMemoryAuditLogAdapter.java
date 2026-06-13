package com.example.shop.infrastructure;

import com.example.shop.domain.AuditLogPort;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InMemoryAuditLogAdapter implements AuditLogPort {
    private static final List<String> AUDIT = new ArrayList<>();

    @Override
    public void log(String message) {
        AUDIT.add(message);
    }

    @Override
    public List<String> getLogs() {
        return new ArrayList<>(AUDIT);
    }
}
