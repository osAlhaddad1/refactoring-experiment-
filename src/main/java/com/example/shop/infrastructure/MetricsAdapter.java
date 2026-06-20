package com.example.shop.infrastructure;

import com.example.shop.domain.MetricsPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetricsAdapter implements MetricsPort {
    private static final Map<String, Integer> METRICS = new ConcurrentHashMap<>();

    @Override
    public void bump(String metric) {
        METRICS.put(metric, METRICS.getOrDefault(metric, 0) + 1);
    }

    @Override
    public Map<String, Integer> getMetrics() {
        return new ConcurrentHashMap<>(METRICS);
    }
}