package com.example.shop.presentation;

import com.example.shop.application.OrderDto;

public class OrderResponse {
    private Long id;
    private Long productId;
    private int quantity;
    private double total;

    public OrderResponse() {}

    public OrderResponse(Long id, Long productId, int quantity, double total) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
    }

    public static OrderResponse fromDto(OrderDto dto) {
        return new OrderResponse(dto.getId(), dto.getProductId(), dto.getQuantity(), dto.getTotal());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
