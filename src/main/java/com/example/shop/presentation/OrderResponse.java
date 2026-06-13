package com.example.shop.presentation;

import java.util.List;

public class OrderResponse {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public double surcharge;
    public String couponCode;
    public List<OrderLineResponse> lines;

    public OrderResponse() {}
    public OrderResponse(Long id, Long customerId, String status, double total, double surcharge, String couponCode, List<OrderLineResponse> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.surcharge = surcharge;
        this.couponCode = couponCode;
        this.lines = lines;
    }
}
