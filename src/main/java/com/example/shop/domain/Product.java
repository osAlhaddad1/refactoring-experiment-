package com.example.shop.domain;

import com.example.shop.domain.exception.BadRequestException;

public class Product {
    private Long id;
    private String name;
    private double price;
    private int stock;

    public Product(Long id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public void reserveStock(int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("quantity must be positive");
        }
        if (this.stock < quantity) {
            throw new BadRequestException("not enough stock");
        }
        this.stock -= quantity;
    }

    public void restock(int quantity) {
        this.stock += quantity;
    }
}