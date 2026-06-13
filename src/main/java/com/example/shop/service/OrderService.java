package com.example.shop.service;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.Product;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public OrderRecord placeOrder(OrderRecord order) {
        if (order.productId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
        Product product = productRepository.findById(order.productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));

        if (order.quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }
        if (product.stock < order.quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
        }

        double total = product.price * order.quantity;
        if (order.quantity >= 10) {
            total = total * 0.9;
        }

        product.stock = product.stock - order.quantity;
        productRepository.save(product);

        order.id = null;
        order.total = total;
        return orderRepository.save(order);
    }

    public OrderRecord getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }
}
