package com.example.shop.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private int quantity;
    private double total;

    public OrderJpaEntity() {}

    public OrderJpaEntity(Long id, Long productId, int quantity, double total) {
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
}
