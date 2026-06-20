package com.example.shop.controller;

import com.example.shop.model.OrderHeader;
import com.example.shop.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderHeader placeOrder(@RequestBody OrderHeader order) {
        return orderService.placeOrder(order);
    }

    @GetMapping("/{id}")
    public OrderHeader getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/{id}/pay")
    public OrderHeader payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    @PostMapping("/{id}/ship")
    public OrderHeader shipOrder(@PathVariable Long id) {
        return orderService.shipOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderHeader cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @PostMapping("/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        return orderService.invoiceOrder(id);
    }
}