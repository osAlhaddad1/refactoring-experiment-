package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderHeaderEntity, Long> {
}