package com.example.shop.domain;

public interface CategoryRepository {
    Category save(Category category);
    Category findById(Long id);
}