package com.example.shop.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProductEntity {
    @Id
    public Long id;
    public String name;
}
