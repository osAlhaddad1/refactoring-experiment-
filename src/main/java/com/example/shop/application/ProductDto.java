package com.example.shop.application;

public class ProductDto {
    public Long id;
    public String name;
    public double price;
    public int stock;

    public ProductDto() {}

    public ProductDto(Long id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}
