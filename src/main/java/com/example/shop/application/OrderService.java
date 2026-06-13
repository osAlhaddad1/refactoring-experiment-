package com.example.shop.application;

import com.example.shop.domain.Order;
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
    public OrderDTO placeOrder(OrderDTO orderDto) {
        Product product = productRepository.findById(orderDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("product not found"));

        if (orderDto.getQuantity() <= 0) {
            throw new InvalidOrderException("quantity must be positive");
        }
        if (product.getStock() < orderDto.getQuantity()) {
            throw new InvalidOrderException("not enough stock");
        }

        double total = Order.calculateTotal(product.getPrice(), orderDto.getQuantity());

        product.decreaseStock(orderDto.getQuantity());
        productRepository.save(product);

        Order order = new Order(null, orderDto.getProductId(), orderDto.getQuantity(), total);
        Order savedOrder = orderRepository.save(order);

        return new OrderDTO(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotal());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("order not found"));
        return new OrderDTO(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}
