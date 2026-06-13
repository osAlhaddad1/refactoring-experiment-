package com.example.shop.service;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.Product;
import com.example.shop.persistence.OrderRepository;
import com.example.shop.persistence.ProductRepository;
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
        return productRepository.save(product);
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

    @Transactional(readOnly = true)
    public OrderRecord getOrder(Long id) {
        OrderRecord order = orderRepository.findById(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found");
        }
        return order;
    }
}