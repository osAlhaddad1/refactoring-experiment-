package com.example.shop.domain;

import java.util.List;

public interface AuditLogPort {
    void log(String message);
    List<String> getLogs();
}
