package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = new OrderEntity(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
        OrderEntity saved = repository.save(entity);
        return new Order(saved.getId(), saved.getProductId(), saved.getQuantity(), saved.getTotal());
    }

    @Override
    public Optional<Order> findById(Long id) {
        return repository.findById(id)
                .map(entity -> new Order(entity.getId(), entity.getProductId(), entity.getQuantity(), entity.getTotal()));
    }
}
