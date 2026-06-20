package com.example.shop.infrastructure;

import com.example.shop.domain.AuditPort;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class InMemoryAuditAdapter implements AuditPort {
    private static final List<String> AUDIT = new ArrayList<>();

    @Override
    public void add(String message) {
        AUDIT.add(message);
    }

    @Override
    public List<String> getAudit() {
        return new ArrayList<>(AUDIT);
    }
}