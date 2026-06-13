package com.example.shop.application;

import java.util.List;

public class OrderHeaderDto {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public double surcharge;
    public String couponCode;
    public List<OrderLineDto> lines;

    public OrderHeaderDto() {}
    public OrderHeaderDto(Long id, Long customerId, String status, double total, double surcharge, String couponCode, List<OrderLineDto> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.surcharge = surcharge;
        this.couponCode = couponCode;
        this.lines = lines;
    }
}
