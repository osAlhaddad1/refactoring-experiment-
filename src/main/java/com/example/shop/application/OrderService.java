package com.example.shop.application;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDto placeOrder(OrderDto orderDto) {
        Product product = productRepository.findById(orderDto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));

        if (orderDto.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }
        if (product.getStock() < orderDto.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
        }

        // business rule: buy 10 or more and you get 10% off the whole line
        double total = product.getPrice() * orderDto.getQuantity();
        if (orderDto.getQuantity() >= 10) {
            total = total * 0.9;
        }

        // stock management: take the items out of stock
        product.decreaseStock(orderDto.getQuantity());
        productRepository.save(product);

        Order order = new Order(null, orderDto.getProductId(), orderDto.getQuantity(), total);
        Order saved = orderRepository.save(order);

        return new OrderDto(saved.getId(), saved.getProductId(), saved.getQuantity(), saved.getTotal());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return new OrderDto(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}
