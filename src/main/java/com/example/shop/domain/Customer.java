package com.example.shop.domain;

public class Customer {
    public Long id;
    public String name;
    public int loyaltyPoints;

    public Customer() {}

    public Customer(Long id, String name, int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.loyaltyPoints = loyaltyPoints;
    }
}
