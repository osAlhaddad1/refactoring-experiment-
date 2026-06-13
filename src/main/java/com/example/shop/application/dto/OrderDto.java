package com.example.shop.application.dto;

import java.util.List;

public class OrderDto {
    private Long id;
    private Long customerId;
    private String status;
    private double total;
    private double surcharge;
    private String couponCode;
    private List<OrderLineDto> lines;

    public OrderDto() {}
    public OrderDto(Long id, Long customerId, String status, double total, double surcharge, String couponCode, List<OrderLineDto> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.surcharge = surcharge;
        this.couponCode = couponCode;
        this.lines = lines;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public double getSurcharge() { return surcharge; }
    public void setSurcharge(double surcharge) { this.surcharge = surcharge; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public List<OrderLineDto> getLines() { return lines; }
    public void setLines(List<OrderLineDto> lines) { this.lines = lines; }
}
