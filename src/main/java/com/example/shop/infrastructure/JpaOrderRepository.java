package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<OrderHeaderEntity, Long> {
}