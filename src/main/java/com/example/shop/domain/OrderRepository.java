package com.example.shop.domain;

public interface OrderRepository {
    OrderHeader save(OrderHeader order);
    OrderHeader findById(Long id);
}