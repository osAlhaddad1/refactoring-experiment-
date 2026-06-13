package com.example.shop.domain;

import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(Long id);
}