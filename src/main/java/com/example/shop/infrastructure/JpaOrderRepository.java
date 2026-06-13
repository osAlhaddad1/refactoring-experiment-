package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

@Repository
public class JpaOrderRepository implements OrderRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.id = order.getId();
        entity.customerId = order.getCustomerId();
        entity.status = order.getStatus();
        entity.total = order.getTotal();
        entity.surcharge = order.getSurcharge();

        if (order.getLines() != null) {
            for (OrderLine line : order.getLines()) {
                OrderLineEntity lineEntity = new OrderLineEntity();
                lineEntity.id = line.getId();
                lineEntity.productId = line.getProductId();
                lineEntity.quantity = line.getQuantity();
                lineEntity.linePrice = line.getLinePrice();
                entity.lines.add(lineEntity);
            }
        }

        if (entity.id == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        order.setId(entity.id);
        if (entity.lines != null && order.getLines() != null) {
            for (int i = 0; i < entity.lines.size(); i++) {
                order.getLines().get(i).setId(entity.lines.get(i).id);
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
        OrderHeader order = new OrderHeader(
                entity.id,
                entity.customerId,
                entity.status,
                entity.total,
                entity.surcharge,
                null,
                lines
        );
        return Optional.of(order);
    }
}