package com.example.shop.application;

public class OrderData {
    public Long id;
    public Long productId;
    public int quantity;
    public double total;

    public OrderData() {}

    public OrderData(Long id, Long productId, int quantity, double total) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
    }
}
