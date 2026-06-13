package com.example.shop.domain;

import java.util.List;

public interface AuditPort {
    void add(String message);
    List<String> getAudit();
}