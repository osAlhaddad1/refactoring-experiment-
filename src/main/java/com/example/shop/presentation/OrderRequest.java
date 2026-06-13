package com.example.shop.presentation;

import java.util.List;

public class OrderRequest {
    private Long customerId;
    private String couponCode;
    private List<OrderLineRequest> lines;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public List<OrderLineRequest> getLines() { return lines; }
    public void setLines(List<OrderLineRequest> lines) { this.lines = lines; }
}
