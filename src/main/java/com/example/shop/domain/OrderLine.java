package com.example.shop.domain;

public class OrderLine {
    public Long id;
    public Long productId;
    public int quantity;
    public double linePrice;

    public OrderLine() {}

    public OrderLine(Long id, Long productId, int quantity, double linePrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.linePrice = linePrice;
    }
}
