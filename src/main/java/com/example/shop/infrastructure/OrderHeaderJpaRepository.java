package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHeaderJpaRepository extends JpaRepository<OrderHeaderJpaEntity, Long> {
}