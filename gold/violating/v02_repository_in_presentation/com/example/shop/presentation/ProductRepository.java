package com.example.shop.presentation;

import org.springframework.data.jpa.repository.JpaRepository;

// VIOLATION (naming): a Spring Data repository must live in ..infrastructure..,
// not in ..presentation..
public interface ProductRepository extends JpaRepository<Product, Long> {
}
