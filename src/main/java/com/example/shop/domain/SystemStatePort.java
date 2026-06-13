package com.example.shop.domain;

import java.util.List;
import java.util.Map;

public interface SystemStatePort {
    void addAuditLog(String message);
    List<String> getAuditLogs();
    void incrementMetric(String metric);
    Map<String, Integer> getMetrics();
    long nextInvoiceNumber();
}