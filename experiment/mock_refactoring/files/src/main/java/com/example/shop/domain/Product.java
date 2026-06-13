package com.example.shop.domain;

// Pure domain entity: no Spring, no JPA.
public class Product {
    public Long id;
    public String name;
    public double price;
    public int stock;

    public boolean hasStock(int quantity) {
        return stock >= quantity;
    }

    public void reduceStock(int quantity) {
        stock = stock - quantity;
    }
}
