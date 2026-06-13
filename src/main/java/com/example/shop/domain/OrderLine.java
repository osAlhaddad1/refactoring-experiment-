package com.example.shop.domain;

public class OrderLine {
    private Long id;
    private Long productId;
    private int quantity;
    private double linePrice;

    public OrderLine(Long id, Long productId, int quantity, double linePrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.linePrice = linePrice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getLinePrice() { return linePrice; }
    public void setLinePrice(double linePrice) { this.linePrice = linePrice; }
}
