package com.example.shop.presentation;

import java.util.Map;

public class ProductDto {
    public Long id;
    public String name;
    public double price;
    public int stock;
    public CategoryDto category;

    public ProductDto() {}

    @SuppressWarnings("unchecked")
    public ProductDto(Map<String, Object> map) {
        if (map != null) {
            this.id = map.get("id") != null ? ((Number) map.get("id")).longValue() : null;
            this.name = (String) map.get("name");
            this.price = map.get("price") != null ? ((Number) map.get("price")).doubleValue() : 0.0;
            this.stock = map.get("stock") != null ? ((Number) map.get("stock")).intValue() : 0;
            if (map.get("category") != null) {
                this.category = new CategoryDto((Map<String, Object>) map.get("category"));
            }
        }
    }
}