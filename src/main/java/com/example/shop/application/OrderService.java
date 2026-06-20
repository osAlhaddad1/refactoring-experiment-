package com.example.shop.application;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepositoryPort;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepositoryPort orderRepository;
    private final ProductRepositoryPort productRepository;

    public OrderService(OrderRepositoryPort orderRepository, ProductRepositoryPort productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDto placeOrder(OrderDto dto) {
        Product product = productRepository.findById(dto.productId)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));

        OrderRecord order = new OrderRecord(null, dto.productId, dto.quantity, 0.0);

        try {
            order.calculateTotal(product.getPrice());
            product.decreaseStock(order.getQuantity());
        } catch (IllegalArgumentException e) {
            throw new OrderValidationException(e.getMessage());
        }

        productRepository.save(product);
        OrderRecord saved = orderRepository.save(order);
        return toDto(saved);
    }

    public OrderDto getOrder(Long id) {
        OrderRecord order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("order not found"));
        return toDto(order);
    }

    private OrderDto toDto(OrderRecord order) {
        return new OrderDto(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}
