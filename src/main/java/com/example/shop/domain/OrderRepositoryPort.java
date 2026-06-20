package com.example.shop.domain;

import java.util.Optional;

public interface OrderRepositoryPort {
    OrderHeader save(OrderHeader order);
    Optional<OrderHeader> findById(Long id);
}