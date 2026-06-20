package com.example.shop.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    public double price;
    public int stock;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    public CategoryEntity category;
}
