package com.example.shop.presentation;

import java.util.List;

public class OrderHeaderDto {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public List<OrderLineDto> lines;

    public OrderHeaderDto() {}

    public OrderHeaderDto(Long id, Long customerId, String status, double total, List<OrderLineDto> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.lines = lines;
    }
}