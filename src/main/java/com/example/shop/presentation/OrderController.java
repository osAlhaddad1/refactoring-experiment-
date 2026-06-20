package com.example.shop.presentation;

import com.example.shop.application.BadRequestException;
import com.example.shop.application.NotFoundException;
import com.example.shop.application.OrderDto;
import com.example.shop.application.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderDto dto = orderService.placeOrder(request.productId, request.quantity);
            return new OrderResponse(dto.id, dto.productId, dto.quantity, dto.total);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        try {
            OrderDto dto = orderService.getOrder(id);
            return new OrderResponse(dto.id, dto.productId, dto.quantity, dto.total);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}