package com.example.shop.presentation;

public class ProductResponse {
    public Long id;
    public String name;
    public double price;
    public int stock;
    public CategoryResponse category;

    public ProductResponse() {}
    public ProductResponse(Long id, String name, double price, int stock, CategoryResponse category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
}
