package com.example.shop.presentation;

public class OrderLineResponse {
    public Long id;
    public Long productId;
    public int quantity;
    public double linePrice;

    public OrderLineResponse() {}
    public OrderLineResponse(Long id, Long productId, int quantity, double linePrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.linePrice = linePrice;
    }
}
