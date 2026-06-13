package com.example.shop.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditAndMetricsService {
    private final List<String> audit = new ArrayList<>();
    private final Map<String, Integer> metrics = new HashMap<>();
    private long invoiceCounter = 0;

    public synchronized void logAudit(String message) {
        audit.add(message);
    }

    public synchronized List<String> getAudit() {
        return audit;
    }

    public synchronized void bumpMetric(String metric) {
        metrics.put(metric, metrics.getOrDefault(metric, 0) + 1);
    }

    public synchronized Map<String, Integer> getMetrics() {
        return metrics;
    }

    public synchronized long incrementAndGetInvoiceCounter() {
        return ++invoiceCounter;
    }
}