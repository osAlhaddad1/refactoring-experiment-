package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderRepository extends JpaRepository<OrderHeaderEntity, Long> {
}
