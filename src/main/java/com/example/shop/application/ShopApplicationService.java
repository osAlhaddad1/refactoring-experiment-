package com.example.shop.application;

import com.example.shop.domain.Order;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShopApplicationService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ShopApplicationService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Product createProduct(String name, double price, int stock) {
        Product product = new Product(null, name, price, stock);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProduct(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Order placeOrder(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("not enough stock");
        }

        product.decreaseStock(quantity);
        productRepository.save(product);

        double total = Order.calculateTotal(product.getPrice(), quantity);
        Order order = new Order(null, productId, quantity, total);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrder(Long id) {
        return orderRepository.findById(id);
    }
}
