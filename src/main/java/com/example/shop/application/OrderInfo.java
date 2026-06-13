package com.example.shop.application;

import java.util.List;

public class OrderInfo {
    private final Long id;
    private final Long customerId;
    private final String status;
    private final double total;
    private final List<OrderLineInfo> lines;

    public OrderInfo(Long id, Long customerId, String status, double total, List<OrderLineInfo> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.lines = lines;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getStatus() { return status; }
    public double getTotal() { return total; }
    public List<OrderLineInfo> getLines() { return lines; }
}