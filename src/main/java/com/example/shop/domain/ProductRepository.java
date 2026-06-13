package com.example.shop.domain;

public interface ProductRepository {
    Product save(Product product);
    Product findById(Long id);
}