package com.example.shop.infrastructure;

import com.example.shop.domain.SystemStatePort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SystemStateAdapter implements SystemStatePort {

    private static final List<String> AUDIT = new ArrayList<>();
    private static final Map<String, Integer> METRICS = new HashMap<>();
    private static long INVOICE_COUNTER = 0;

    @Override
    public synchronized void addAuditLog(String message) {
        AUDIT.add(message);
    }

    @Override
    public synchronized List<String> getAuditLogs() {
        return new ArrayList<>(AUDIT);
    }

    @Override
    public synchronized void incrementMetric(String metric) {
        METRICS.put(metric, METRICS.getOrDefault(metric, 0) + 1);
    }

    @Override
    public synchronized Map<String, Integer> getMetrics() {
        return new HashMap<>(METRICS);
    }

    @Override
    public synchronized long nextInvoiceNumber() {
        return ++INVOICE_COUNTER;
    }
}