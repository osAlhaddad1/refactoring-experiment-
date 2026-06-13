package com.example.shop.application.dto;

public class ProductDto {
    private Long id;
    private String name;
    private double price;
    private int stock;
    private CategoryDto category;

    public ProductDto() {}
    public ProductDto(Long id, String name, double price, int stock, CategoryDto category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public CategoryDto getCategory() { return category; }
    public void setCategory(CategoryDto category) { this.category = category; }
}
