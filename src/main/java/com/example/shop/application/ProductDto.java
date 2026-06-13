package com.example.shop.application;

public class ProductDto {
    public Long id;
    public String name;
    public double price;
    public int stock;
    public CategoryDto category;

    public ProductDto() {}
    public ProductDto(Long id, String name, double price, int stock, CategoryDto category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
}
