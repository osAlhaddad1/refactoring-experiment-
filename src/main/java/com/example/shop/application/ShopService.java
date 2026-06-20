package com.example.shop.application;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ShopService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ProductData createProduct(String name, double price, int stock) {
        Product product = new Product(null, name, price, stock);
        Product saved = productRepository.save(product);
        return new ProductData(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    public ProductData getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return new ProductData(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }

    public List<ProductData> listProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductData(p.getId(), p.getName(), p.getPrice(), p.getStock()))
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderData placeOrder(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));

        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }
        if (product.getStock() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
        }

        OrderRecord order = new OrderRecord(null, productId, quantity, 0.0);
        order.calculateTotal(product.getPrice());
        product.decreaseStock(quantity);

        productRepository.save(product);
        OrderRecord savedOrder = orderRepository.save(order);

        return new OrderData(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotal());
    }

    public OrderData getOrder(Long id) {
        OrderRecord order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return new OrderData(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}
