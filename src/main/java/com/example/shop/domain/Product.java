package com.example.shop.domain;

public class Product {
    public Long id;
    public String name;
    public double price;
    public int stock;
    public Category category;

    public Product() {}

    public Product(Long id, String name, double price, int stock, Category category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
}
