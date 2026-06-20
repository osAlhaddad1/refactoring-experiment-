package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = toJpaEntity(order);
        if (entity.getId() == null) {
            em.persist(entity);
            em.flush();
            order.setId(entity.getId());
            for (int i = 0; i < entity.getLines().size(); i++) {
                order.getLines().get(i).setId(entity.getLines().get(i).getId());
            }
        } else {
            entity = em.merge(entity);
            em.flush();
        }
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        OrderJpaEntity entity = em.find(OrderJpaEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    private OrderJpaEntity toJpaEntity(Order order) {
        List<OrderLineJpaEntity> lines = order.getLines().stream()
                .map(line -> new OrderLineJpaEntity(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderJpaEntity(order.getId(), order.getCustomerId(), order.getStatus(), order.getTotal(), lines);
    }

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderLine> lines = entity.getLines().stream()
                .map(line -> new OrderLine(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new Order(entity.getId(), entity.getCustomerId(), entity.getStatus(), entity.getTotal(), lines);
    }
}