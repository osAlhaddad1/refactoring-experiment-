package com.example.shop.infrastructure;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public OrderRecord save(OrderRecord order) {
        OrderJpaEntity jpaEntity = toJpa(order);
        if (jpaEntity.getId() == null) {
            em.persist(jpaEntity);
        } else {
            jpaEntity = em.merge(jpaEntity);
        }
        return toDomain(jpaEntity);
    }

    @Override
    public Optional<OrderRecord> findById(Long id) {
        OrderJpaEntity jpaEntity = em.find(OrderJpaEntity.class, id);
        return Optional.ofNullable(jpaEntity).map(this::toDomain);
    }

    private OrderJpaEntity toJpa(OrderRecord domain) {
        if (domain == null) return null;
        return new OrderJpaEntity(domain.getId(), domain.getProductId(), domain.getQuantity(), domain.getTotal());
    }

    private OrderRecord toDomain(OrderJpaEntity jpa) {
        if (jpa == null) return null;
        return new OrderRecord(jpa.getId(), jpa.getProductId(), jpa.getQuantity(), jpa.getTotal());
    }
}
