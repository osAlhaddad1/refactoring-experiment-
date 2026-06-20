package com.example.shop.domain;

import java.util.Optional;

public interface OrderRepositoryPort {
    OrderRecord save(OrderRecord order);
    Optional<OrderRecord> findById(Long id);
}
