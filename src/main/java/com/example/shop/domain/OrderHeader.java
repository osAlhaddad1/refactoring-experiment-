package com.example.shop.domain;

import com.example.shop.domain.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;

public class OrderHeader {
    private Long id;
    private Long customerId;
    private String status;
    private double total;
    private List<OrderLine> lines = new ArrayList<>();

    public OrderHeader(Long id, Long customerId, String status, double total, List<OrderLine> lines) {
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
    public List<OrderLine> getLines() { return lines; }
    public void setLines(List<OrderLine> lines) { this.lines = lines; }

    public void pay() {
        if (!"NEW".equals(status)) {
            throw new BadRequestException("only NEW orders can be paid");
        }
        this.status = "PAID";
    }

    public void ship() {
        if (!"PAID".equals(status)) {
            throw new BadRequestException("only PAID orders can be shipped");
        }
        this.status = "SHIPPED";
    }

    public void cancel() {
        if ("SHIPPED".equals(status) || "CANCELLED".equals(status)) {
            throw new BadRequestException("cannot cancel a " + status + " order");
        }
        this.status = "CANCELLED";
    }
}