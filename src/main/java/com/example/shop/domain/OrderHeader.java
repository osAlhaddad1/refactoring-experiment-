package com.example.shop.domain;

import java.util.ArrayList;
import java.util.List;

public class OrderHeader {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public double surcharge;
    public String couponCode;
    public List<OrderLine> lines = new ArrayList<>();

    public OrderHeader() {}
    public OrderHeader(Long id, Long customerId, String status, double total, double surcharge, String couponCode, List<OrderLine> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.surcharge = surcharge;
        this.couponCode = couponCode;
        this.lines = lines != null ? lines : new ArrayList<>();
    }
}