package com.example.shop.presentation;

import com.example.shop.infrastructure.OrderAdapter;

// VIOLATION (layered): presentation must not reach into ..infrastructure..
public class OrderController {

    private final OrderAdapter adapter = new OrderAdapter();

    public Long firstId() {
        return adapter.id();
    }
}
