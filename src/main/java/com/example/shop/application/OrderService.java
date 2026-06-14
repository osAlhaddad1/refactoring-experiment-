package com.example.shop.application;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepository;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
                .orElseThrow(() -> new ProductNotFoundException("product not found"));

        if (orderDto.getQuantity() <= 0) {
            throw new InvalidOrderException("quantity must be positive");
        }
        if (product.getStock() < orderDto.getQuantity()) {
            throw new InvalidOrderException("not enough stock");
        }

        // business rule: buy 10 or more and you get 10% off the whole line
        double total = product.getPrice() * orderDto.getQuantity();
        if (orderDto.getQuantity() >= 10) {
            total = total * 0.9;
        }

        // stock management: take the items out of stock
        product.setStock(product.getStock() - orderDto.getQuantity());
        productRepository.save(product);

        OrderRecord order = new OrderRecord(null, orderDto.getProductId(), orderDto.getQuantity(), total);
        OrderRecord savedOrder = orderRepository.save(order);

        return new OrderDto(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotal());
    }

    public Optional<OrderDto> getOrder(Long id) {
        return orderRepository.findById(id)
                .map(o -> new OrderDto(o.getId(), o.getProductId(), o.getQuantity(), o.getTotal()));
    }
}
