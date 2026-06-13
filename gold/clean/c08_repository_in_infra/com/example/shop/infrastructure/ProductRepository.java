package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

// CLEAN: a Spring Data repository correctly placed in ..infrastructure..
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
