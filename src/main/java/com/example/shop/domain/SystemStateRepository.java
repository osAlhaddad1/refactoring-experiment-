package com.example.shop.domain;

import java.util.List;
import java.util.Map;

public interface SystemStateRepository {
    void addAudit(String message);
    List<String> getAudit();
    void bumpMetric(String metric);
    Map<String, Integer> getMetrics();
    long incrementInvoiceCounter();
}
