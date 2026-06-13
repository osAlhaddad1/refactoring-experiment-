package com.example.shop.presentation;

import java.util.Map;

public class CategoryDto {
    public Long id;
    public String name;

    public CategoryDto() {}

    public CategoryDto(Map<String, Object> map) {
        if (map != null) {
            this.id = map.get("id") != null ? ((Number) map.get("id")).longValue() : null;
            this.name = (String) map.get("name");
        }
    }
}