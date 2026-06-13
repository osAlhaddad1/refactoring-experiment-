package com.example.shop.presentation;

public class CustomerResponse {
    private Long id;
    private String name;
    private int loyaltyPoints;

    public CustomerResponse() {}
    public CustomerResponse(Long id, String name, int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.loyaltyPoints = loyaltyPoints;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
}
