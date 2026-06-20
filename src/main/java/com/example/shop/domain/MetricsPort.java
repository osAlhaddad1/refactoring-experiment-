package com.example.shop.domain;

import java.util.Map;

public interface MetricsPort {
    void bump(String metric);
    Map<String, Integer> getMetrics();
}