package com.example.shop.application;

public class ProductData {
    public Long id;
    public String name;
    public double price;
    public int stock;

    public ProductData() {}

    public ProductData(Long id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}
