package com.example.shop.application;

public class OrderLineInfo {
    private final Long id;
    private final Long productId;
    private final int quantity;
    private final double linePrice;

    public OrderLineInfo(Long id, Long productId, int quantity, double linePrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.linePrice = linePrice;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getLinePrice() { return linePrice; }
}