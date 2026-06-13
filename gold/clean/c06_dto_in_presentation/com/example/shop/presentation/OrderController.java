package com.example.shop.presentation;

import com.example.shop.application.OrderService;

// CLEAN: presentation -> application, and maps to its own DTO.
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderResponse get(long id) {
        OrderResponse response = new OrderResponse();
        response.orderId = id;
        response.total = orderService.totalFor(id);
        return response;
    }
}
