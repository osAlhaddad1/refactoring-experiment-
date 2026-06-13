package com.example.shop.presentation;

import com.example.shop.application.ProductDto;

public class ProductResponse {
    private Long id;
    private String name;
    private double price;
    private int stock;

    public ProductResponse() {}

    public ProductResponse(Long id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public static ProductResponse fromDto(ProductDto dto) {
        return new ProductResponse(dto.getId(), dto.getName(), dto.getPrice(), dto.getStock());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
