package com.example.shop.presentation;

public class CustomerDto {
    public Long id;
    public String name;
    public int loyaltyPoints;

    public CustomerDto() {}

    public CustomerDto(Long id, String name, int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.loyaltyPoints = loyaltyPoints;
    }
}