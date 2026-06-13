package com.example.shop.domain;

import java.util.List;
import java.util.Optional;

// Repository PORT interface. The JPA implementation lives in infrastructure.
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
}
