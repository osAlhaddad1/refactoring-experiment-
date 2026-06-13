package com.example.shop.presentation;

import com.example.shop.application.OrderData;

public class OrderDto {
    public Long id;
    public Long productId;
    public int quantity;
    public double total;

    public OrderDto() {}

    public OrderDto(Long id, Long productId, int quantity, double total) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
    }

    public static OrderDto fromData(OrderData data) {
        return new OrderDto(data.id, data.productId, data.quantity, data.total);
    }
}