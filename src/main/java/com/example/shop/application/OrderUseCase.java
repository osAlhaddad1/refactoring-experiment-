package com.example.shop.application;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepositoryPort;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final ProductRepositoryPort productRepository;

    public OrderUseCase(OrderRepositoryPort orderRepository, ProductRepositoryPort productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDto placeOrder(OrderDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("product not found"));

        if (dto.getQuantity() <= 0) {
            throw new BadRequestException("quantity must be positive");
        }
        if (product.getStock() < dto.getQuantity()) {
            throw new BadRequestException("not enough stock");
        }

        double total = OrderRecord.calculateTotal(product.getPrice(), dto.getQuantity());
        product.decreaseStock(dto.getQuantity());

        productRepository.save(product);

        OrderRecord order = new OrderRecord(null, dto.getProductId(), dto.getQuantity(), total);
        OrderRecord saved = orderRepository.save(order);

        return new OrderDto(saved.getId(), saved.getProductId(), saved.getQuantity(), saved.getTotal());
    }

    public OrderDto getOrder(Long id) {
        OrderRecord order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        return new OrderDto(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }
}
