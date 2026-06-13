package com.example.shop.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

// VIOLATION (domain purity): the domain must not import jakarta.persistence.
@Entity
public class Product {
    @Id
    public Long id;
    public String name;
}
