package com.example.shop.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditServiceImpl implements AuditService {
    private final List<String> audit = new ArrayList<>();
    private final Map<String, Integer> metrics = new HashMap<>();
    private long invoiceCounter = 0;

    @Override
    public synchronized void log(String message) {
        audit.add(message);
    }

    @Override
    public synchronized void bump(String metric) {
        metrics.put(metric, metrics.getOrDefault(metric, 0) + 1);
    }

    @Override
    public synchronized long incrementInvoiceCounter() {
        return ++invoiceCounter;
    }

    @Override
    public synchronized List<String> getAuditLog() {
        return audit;
    }

    @Override
    public synchronized Map<String, Integer> getMetrics() {
        return metrics;
    }
}