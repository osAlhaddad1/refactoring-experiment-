package com.example.shop.persistence;

import com.example.shop.domain.OrderRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    @PersistenceContext
    private EntityManager em;

    public OrderRecord save(OrderRecord order) {
        if (order.id == null) {
            em.persist(order);
            return order;
        } else {
            return em.merge(order);
        }
    }

    public OrderRecord findById(Long id) {
        return em.find(OrderRecord.class, id);
    }
}