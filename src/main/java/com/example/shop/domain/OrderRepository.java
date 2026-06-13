package com.example.shop.domain;

import java.util.Optional;

// Repository PORT interface.
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
}
