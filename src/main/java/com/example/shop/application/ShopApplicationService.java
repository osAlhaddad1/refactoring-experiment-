package com.example.shop.application;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopApplicationService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ShopApplicationService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ProductData createProduct(ProductData data) {
        Product product = new Product(null, data.name, data.price, data.stock);
        Product saved = productRepository.save(product);
        return new ProductData(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    public Optional<ProductData> getProduct(Long id) {
        return productRepository.findById(id)
                .map(p -> new ProductData(p.getId(), p.getName(), p.getPrice(), p.getStock()));
    }

    public List<ProductData> listProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductData(p.getId(), p.getName(), p.getPrice(), p.getStock()))
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderData placeOrder(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));

        if (product.getStock() < quantity) {
            throw new IllegalStateException("not enough stock");
        }

        double total = product.calculateTotal(quantity);
        product.decreaseStock(quantity);

        productRepository.save(product);

        Order order = new Order(null, productId, quantity, total);
        Order savedOrder = orderRepository.save(order);

        return new OrderData(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotal());
    }

    public Optional<OrderData> getOrder(Long id) {
        return orderRepository.findById(id)
                .map(o -> new OrderData(o.getId(), o.getProductId(), o.getQuantity(), o.getTotal()));
    }
}