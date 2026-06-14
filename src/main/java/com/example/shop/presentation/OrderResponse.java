package com.example.shop.presentation;

public class OrderResponse {
    public Long id;
    public Long productId;
    public int quantity;
    public double total;

    public OrderResponse() {}

    public OrderResponse(Long id, Long productId, int quantity, double total) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
    }
}
