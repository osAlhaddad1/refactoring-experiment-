package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity;
        if (order.id == null) {
            entity = new OrderHeaderEntity();
            entity.customerId = order.customerId;
            entity.status = order.status;
            entity.total = order.total;
            entity.surcharge = order.surcharge;
            if (order.lines != null) {
                for (OrderLine line : order.lines) {
                    OrderLineEntity lineEntity = new OrderLineEntity();
                    lineEntity.productId = line.productId;
                    lineEntity.quantity = line.quantity;
                    lineEntity.linePrice = line.linePrice;
                    entity.lines.add(lineEntity);
                }
            }
            em.persist(entity);
        } else {
            entity = em.find(OrderHeaderEntity.class, order.id);
            if (entity != null) {
                entity.status = order.status;
                entity.total = order.total;
                entity.surcharge = order.surcharge;
                entity = em.merge(entity);
            }
        }
        order.id = entity.id;
        if (order.lines != null && entity.lines != null) {
            for (int i = 0; i < Math.min(order.lines.size(), entity.lines.size()); i++) {
                order.lines.get(i).id = entity.lines.get(i).id;
            }
        }
        return order;
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        OrderHeaderEntity entity = em.find(OrderHeaderEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        List<OrderLine> lines = new ArrayList<>();
        if (entity.lines != null) {
            for (OrderLineEntity lineEntity : entity.lines) {
                lines.add(new OrderLine(lineEntity.id, lineEntity.productId, lineEntity.quantity, lineEntity.linePrice));
            }
        }
        return Optional.of(new OrderHeader(entity.id, entity.customerId, entity.status, entity.total, entity.surcharge, null, lines));
    }
}
