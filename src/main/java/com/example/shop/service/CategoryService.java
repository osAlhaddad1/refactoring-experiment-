package com.example.shop.service;

import com.example.shop.model.Category;

public interface CategoryService {
    Category createCategory(Category category);
    Category getCategory(Long id);
}