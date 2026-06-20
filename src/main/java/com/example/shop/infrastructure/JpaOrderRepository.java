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
        if (order.getId() == null) {
            OrderJpaEntity jpaEntity = new OrderJpaEntity(
                    null,
                    order.getProductId(),
                    order.getQuantity(),
                    order.getTotal()
            );
            em.persist(jpaEntity);
            order.setId(jpaEntity.getId());
        } else {
            OrderJpaEntity jpaEntity = em.find(OrderJpaEntity.class, order.getId());
            if (jpaEntity != null) {
                jpaEntity.setProductId(order.getProductId());
                jpaEntity.setQuantity(order.getQuantity());
                jpaEntity.setTotal(order.getTotal());
                em.merge(jpaEntity);
            }
        }
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        OrderJpaEntity jpaEntity = em.find(OrderJpaEntity.class, id);
        if (jpaEntity == null) {
            return Optional.empty();
        }
        return Optional.of(new Order(
                jpaEntity.getId(),
                jpaEntity.getProductId(),
                jpaEntity.getQuantity(),
                jpaEntity.getTotal()
        ));
    }
}
