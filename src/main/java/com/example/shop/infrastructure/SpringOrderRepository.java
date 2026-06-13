package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringOrderRepository extends JpaRepository<OrderHeaderEntity, Long> {
}