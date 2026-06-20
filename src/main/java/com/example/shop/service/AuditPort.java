package com.example.shop.service;

import java.util.List;

public interface AuditPort {
    void log(String message);
    List<String> getLogs();
}