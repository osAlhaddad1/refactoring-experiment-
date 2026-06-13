package com.example.shop.service;

import java.util.List;
import java.util.Map;

public interface AuditService {
    void log(String message);
    void bump(String metric);
    long incrementInvoiceCounter();
    List<String> getAuditLog();
    Map<String, Integer> getMetrics();
}