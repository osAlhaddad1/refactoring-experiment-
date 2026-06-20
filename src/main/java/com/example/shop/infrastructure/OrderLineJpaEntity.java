package com.example.shop.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "order_lines")
public class OrderLineJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long productId;
    public int quantity;
    public double linePrice;
}