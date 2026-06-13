package com.example.shop.presentation;

public class CustomerResponse {
    public Long id;
    public String name;
    public int loyaltyPoints;

    public CustomerResponse() {}
    public CustomerResponse(Long id, String name, int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.loyaltyPoints = loyaltyPoints;
    }
}
