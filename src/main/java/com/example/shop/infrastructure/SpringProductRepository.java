package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringProductRepository extends JpaRepository<ProductEntity, Long> {
}