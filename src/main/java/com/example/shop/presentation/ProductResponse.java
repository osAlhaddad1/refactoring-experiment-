package com.example.shop.presentation;

public class ProductResponse {
    public Long id;
    public String name;
    public double price;
    public int stock;

    public ProductResponse() {}

    public ProductResponse(Long id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}