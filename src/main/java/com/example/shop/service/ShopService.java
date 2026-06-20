package com.example.shop.service;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.Product;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ShopService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ShopService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Product createProduct(Product product) {
        product.id = null;
        productRepository.persist(product);
        return product;
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
        return product;
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    public OrderRecord placeOrder(OrderRecord order) {
        Product product = productRepository.findById(order.productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
        if (order.quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }
        if (product.stock < order.quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
        }

        // business rule: buy 10 or more and you get 10% off the whole line
        double total = product.price * order.quantity;
        if (order.quantity >= 10) {
            total = total * 0.9;
        }

        // stock management: take the items out of stock
        product.stock = product.stock - order.quantity;

        order.id = null;
        order.total = total;
        orderRepository.persist(order);
        return order;
    }

    @Transactional(readOnly = true)
    public OrderRecord getOrder(Long id) {
        OrderRecord order = orderRepository.findById(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found");
        }
        return order;
    }
}