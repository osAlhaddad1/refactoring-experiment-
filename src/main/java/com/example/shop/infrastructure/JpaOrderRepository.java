package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaOrderRepository implements OrderRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = toEntity(order);
        if (entity.getId() == null) {
            em.persist(entity);
        }
        else {
            entity = em.merge(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<Order> findById(Long id) {
        OrderJpaEntity entity = em.find(OrderJpaEntity.class, id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    private OrderJpaEntity toEntity(Order order) {
        return new OrderJpaEntity(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
    }

    private Order toDomain(OrderJpaEntity entity) {
        return new Order(entity.getId(), entity.getProductId(), entity.getQuantity(), entity.getTotal());
    }
}
