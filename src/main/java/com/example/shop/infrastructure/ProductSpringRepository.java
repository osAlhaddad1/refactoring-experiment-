package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSpringRepository extends JpaRepository<ProductJpaEntity, Long> {
}
