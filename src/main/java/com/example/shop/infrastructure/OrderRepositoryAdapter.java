package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpa;

    public OrderRepositoryAdapter(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    public Order save(Order order) {
        return toDomain(jpa.save(toEntity(order)));
    }

    public Optional<Order> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    private OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.id = order.id;
        entity.productId = order.productId;
        entity.quantity = order.quantity;
        entity.total = order.total;
        return entity;
    }

    private Order toDomain(OrderEntity entity) {
        Order order = new Order();
        order.id = entity.id;
        order.productId = entity.productId;
        order.quantity = entity.quantity;
        order.total = entity.total;
        return order;
    }
}
