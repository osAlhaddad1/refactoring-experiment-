package com.example.shop.infrastructure;

import com.example.shop.domain.SystemStateRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SystemStateRepositoryAdapter implements SystemStateRepository {
    private static final List<String> AUDIT = new ArrayList<>();
    private static final Map<String, Integer> METRICS = new HashMap<>();
    private static long INVOICE_COUNTER = 0;

    @Override
    public synchronized void addAudit(String message) {
        AUDIT.add(message);
    }

    @Override
    public synchronized List<String> getAudit() {
        return new ArrayList<>(AUDIT);
    }

    @Override
    public synchronized void bumpMetric(String metric) {
        METRICS.put(metric, METRICS.getOrDefault(metric, 0) + 1);
    }

    @Override
    public synchronized Map<String, Integer> getMetrics() {
        return new HashMap<>(METRICS);
    }

    @Override
    public synchronized long incrementInvoiceCounter() {
        return ++INVOICE_COUNTER;
    }
}
