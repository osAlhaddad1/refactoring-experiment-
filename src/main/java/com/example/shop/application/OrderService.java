package com.example.shop.application;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
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
                .orElseThrow(() -> new ProductNotFoundException("product not found"));

        if (quantity <= 0) {
            throw new InvalidQuantityException("quantity must be positive");
        }

        try {
            product.decreaseStock(quantity);
        } catch (IllegalArgumentException e) {
            throw new InsufficientStockException("not enough stock");
        }

        productRepository.save(product);

        Order order = new Order(null, productId, quantity, 0.0);
        order.calculateTotal(product.getPrice());
        Order savedOrder = orderRepository.save(order);

        return new OrderDto(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotal());
    }

    public OrderDto getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("order not found"));
        return new OrderDto(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}
