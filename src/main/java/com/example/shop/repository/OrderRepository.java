package com.example.shop.repository;

import com.example.shop.domain.OrderRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(OrderRecord order) {
        em.persist(order);
    }

    public OrderRecord findById(Long id) {
        return em.find(OrderRecord.class, id);
    }
}