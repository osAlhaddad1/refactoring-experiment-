package com.example.shop.domain;

import java.util.List;

public interface AuditLog {
    void log(String message);
    List<String> getLogs();
}