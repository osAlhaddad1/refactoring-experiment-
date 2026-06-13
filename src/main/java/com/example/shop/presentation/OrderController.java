package com.example.shop.presentation;

import com.example.shop.domain.OrderRecord;
import com.example.shop.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public OrderRecord placeOrder(@RequestBody OrderRecord order) {
        return orderService.placeOrder(order);
    }

    @GetMapping("/orders/{id}")
    public OrderRecord getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }
}
