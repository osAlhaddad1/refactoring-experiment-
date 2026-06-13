package com.example.shop.infrastructure;

import com.example.shop.domain.AuditPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AuditAdapter implements AuditPort {
    private static final List<String> AUDIT = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void add(String message) {
        AUDIT.add(message);
    }

    @Override
    public List<String> getAuditLog() {
        return new ArrayList<>(AUDIT);
    }
}