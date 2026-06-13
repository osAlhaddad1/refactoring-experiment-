package com.example.shop.domain;

import java.util.List;

public interface AuditLog {
    void record(String message);
    List<String> getLogs();
}
