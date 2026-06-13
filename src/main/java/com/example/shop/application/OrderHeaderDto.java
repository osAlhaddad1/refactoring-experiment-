package com.example.shop.application;

import java.util.ArrayList;
import java.util.List;

public class OrderHeaderDto {
    private Long id;
    private Long customerId;
    private String status;
    private double total;
    private List<OrderLineDto> lines = new ArrayList<>();

    public OrderHeaderDto() {}

    public OrderHeaderDto(Long id, Long customerId, String status, double total, List<OrderLineDto> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.lines = lines != null ? lines : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public List<OrderLineDto> getLines() { return lines; }
    public void setLines(List<OrderLineDto> lines) { this.lines = lines; }
}