package com.example.shop.application;

public class ProductDto {
    private final Long id;
    private final String name;
    private final double price;
    private final int stock;

    public ProductDto(Long id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
}
