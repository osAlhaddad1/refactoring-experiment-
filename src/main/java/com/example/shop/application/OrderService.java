package com.example.shop.application;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepository;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDto placeOrder(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product not found"));

        if (quantity <= 0) {
            throw new InvalidOrderException("quantity must be positive");
        }
        if (product.getStock() < quantity) {
            throw new InvalidOrderException("not enough stock");
        }

        double total = product.getPrice() * quantity;
        if (quantity >= 10) {
            total = total * 0.9;
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        OrderRecord order = new OrderRecord(null, productId, quantity, total);
        OrderRecord savedOrder = orderRepository.save(order);

        return new OrderDto(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotal());
    }

    public OrderDto getOrder(Long id) {
        OrderRecord order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));
        return new OrderDto(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}
