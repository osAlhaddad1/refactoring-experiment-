package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository jpaRepository;

    public OrderRepositoryAdapter(SpringDataOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
        OrderJpaEntity saved = jpaRepository.save(entity);
        order.setId(saved.getId());
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id)
                .map(entity -> new Order(entity.getId(), entity.getProductId(), entity.getQuantity(), entity.getTotal()));
    }
}