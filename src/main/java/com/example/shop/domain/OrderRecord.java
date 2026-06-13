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
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public static double calculateTotal(double price, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        double total = price * quantity;
        if (quantity >= 10) {
            total = total * 0.9;
        }
        return total;
    }
}
