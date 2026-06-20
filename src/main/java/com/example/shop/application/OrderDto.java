package com.example.shop.application;

public class OrderDto {
    private final Long id;
    private final Long productId;
    private final int quantity;
    private final double total;

    public OrderDto(Long id, Long productId, int quantity, double total) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return total; }
}
