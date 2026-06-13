package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringCategoryRepository extends JpaRepository<CategoryEntity, Long> {
}