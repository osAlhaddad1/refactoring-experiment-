package com.example.shop.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class CustomerJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    public int loyaltyPoints;
}