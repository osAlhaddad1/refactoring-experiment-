package com.example.shop.domain;

import java.util.List;

public interface AuditPort {
    void log(String message);
    List<String> getLogs();
}