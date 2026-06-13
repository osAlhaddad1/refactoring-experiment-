package com.example.shop.application;

public class CustomerInfo {
    private final Long id;
    private final String name;
    private final int loyaltyPoints;

    public CustomerInfo(Long id, String name, int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.loyaltyPoints = loyaltyPoints;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
}