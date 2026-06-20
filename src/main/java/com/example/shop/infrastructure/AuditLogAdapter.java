package com.example.shop.infrastructure;

import com.example.shop.domain.AuditLog;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuditLogAdapter implements AuditLog {
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
