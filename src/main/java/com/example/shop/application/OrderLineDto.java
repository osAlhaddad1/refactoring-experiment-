package com.example.shop.application;

public class OrderLineDto {
    public Long id;
    public Long productId;
    public int quantity;
    public double linePrice;

    public OrderLineDto() {}
    public OrderLineDto(Long id, Long productId, int quantity, double linePrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.linePrice = linePrice;
    }
}
