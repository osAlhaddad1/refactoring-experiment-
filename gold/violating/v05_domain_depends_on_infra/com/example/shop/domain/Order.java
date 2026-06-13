package com.example.shop.domain;

import com.example.shop.infrastructure.OrderEntity;

// VIOLATION (domain purity + layered): the domain must not depend on
// ..infrastructure..
public class Order {
    public OrderEntity entity;
}
