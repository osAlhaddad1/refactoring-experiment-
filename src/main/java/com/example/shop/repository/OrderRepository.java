package com.example.shop.repository;

import com.example.shop.domain.OrderHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderHeader, Long> {
}