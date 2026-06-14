package com.example.shop.application;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShopService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ShopService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product(null, dto.name, dto.price, dto.stock);
        Product saved = productRepository.save(product);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }

    public List<ProductDto> listProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()))
                .collect(Collectors.toList());
    }

    public OrderDto placeOrder(OrderDto dto) {
        Product product = productRepository.findById(dto.productId)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));

        if (dto.quantity <= 0) {
            throw new InvalidQuantityException("quantity must be positive");
        }
        if (product.getStock() < dto.quantity) {
            throw new InsufficientStockException("not enough stock");
        }

        // business rule: buy 10 or more and you get 10% off the whole line
        double total = product.getPrice() * dto.quantity;
        if (dto.quantity >= 10) {
            total = total * 0.9;
        }

        // stock management: take the items out of stock
        product.setStock(product.getStock() - dto.quantity);
        productRepository.save(product);

        OrderRecord order = new OrderRecord(null, dto.productId, dto.quantity, total);
        OrderRecord savedOrder = orderRepository.save(order);

        return new OrderDto(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotal());
    }

    public OrderDto getOrder(Long id) {
        OrderRecord order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("order not found"));
        return new OrderDto(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}
