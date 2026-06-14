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
    public OrderDto placeOrder(Long productId, int quantity) {
        if (productId == null) {
            throw new NotFoundException("product not found");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("product not found"));

        if (quantity <= 0) {
            throw new BadRequestException("quantity must be positive");
        }
        if (product.getStock() < quantity) {
            throw new BadRequestException("not enough stock");
        }

        double total = product.getPrice() * quantity;
        if (quantity >= 10) {
            total = total * 0.9;
        }

        product.decreaseStock(quantity);
        productRepository.save(product);

        Order order = new Order(null, productId, quantity, total);
        Order saved = orderRepository.save(order);

        return new OrderDto(saved.getId(), saved.getProductId(), saved.getQuantity(), saved.getTotal());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        return new OrderDto(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}