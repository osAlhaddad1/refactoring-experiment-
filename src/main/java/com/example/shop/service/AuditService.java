package com.example.shop.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {
    private static final List<String> AUDIT = new ArrayList<>();

    public void log(String message) {
        AUDIT.add(message);
    }

    public List<String> getAuditLog() {
        return AUDIT;
    }
}