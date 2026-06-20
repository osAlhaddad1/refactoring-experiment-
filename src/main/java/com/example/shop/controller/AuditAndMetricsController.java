package com.example.shop.controller;

import com.example.shop.service.AuditAndMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AuditAndMetricsController {
    private final AuditAndMetricsService auditAndMetricsService;

    public AuditAndMetricsController(AuditAndMetricsService auditAndMetricsService) {
        this.auditAndMetricsService = auditAndMetricsService;
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return auditAndMetricsService.getAudit();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return auditAndMetricsService.getMetrics();
    }
}