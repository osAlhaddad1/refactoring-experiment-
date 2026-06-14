package com.example.shop.application;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    public ProductDto createProduct(ProductDto productDto) {
        Product product = new Product(null, productDto.getName(), productDto.getPrice(), productDto.getStock());
        Product saved = productRepository.save(product);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    public Optional<ProductDto> getProduct(Long id) {
        return productRepository.findById(id)
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()));
    }

    public List<ProductDto> listProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()))
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto placeOrder(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));

        if (quantity <= 0) {
            throw new InvalidQuantityException("quantity must be positive");
        }
        if (product.getStock() < quantity) {
            throw new InsufficientStockException("not enough stock");
        }

        double total = Order.calculateTotal(product.getPrice(), quantity);
        product.decreaseStock(quantity);
        productRepository.save(product);

        Order order = new Order(null, productId, quantity, total);
        Order savedOrder = orderRepository.save(order);
        return new OrderDto(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotal());
    }

    public Optional<OrderDto> getOrder(Long id) {
        return orderRepository.findById(id)
                .map(o -> new OrderDto(o.getId(), o.getProductId(), o.getQuantity(), o.getTotal()));
    }
}