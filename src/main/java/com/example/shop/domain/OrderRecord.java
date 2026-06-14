package com.example.shop.domain;

public class OrderRecord {
    private Long id;
    private Long productId;
    private int quantity;
    private double total;

    public OrderRecord(Long id, Long productId, int quantity, double total) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return total; }

    public void setId(Long id) { this.id = id; }
    public void setTotal(double total) { this.total = total; }
}
