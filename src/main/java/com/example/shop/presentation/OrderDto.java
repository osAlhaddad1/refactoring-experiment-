package com.example.shop.presentation;

import java.util.ArrayList;
import java.util.List;

public class OrderDto {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public List<OrderLineDto> lines = new ArrayList<>();

    public OrderDto() {}

    public OrderDto(Long id, Long customerId, String status, double total, List<OrderLineDto> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.lines = lines != null ? lines : new ArrayList<>();
    }
}