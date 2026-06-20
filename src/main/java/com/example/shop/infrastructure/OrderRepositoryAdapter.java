package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
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
    public Order save(Order order) {
        OrderEntity entity;
        if (order.getId() == null) {
            entity = new OrderEntity();
        } else {
            entity = em.find(OrderEntity.class, order.getId());
            if (entity == null) {
                entity = new OrderEntity();
            }
        }
        entity.customerId = order.getCustomerId();
        entity.status = order.getStatus();
        entity.total = order.getTotal();

        List<OrderLineEntity> lineEntities = new ArrayList<>();
        if (order.getLines() != null) {
            for (OrderLine line : order.getLines()) {
                OrderLineEntity le = new OrderLineEntity();
                le.id = line.getId();
                le.productId = line.getProductId();
                le.quantity = line.getQuantity();
                le.linePrice = line.getLinePrice();
                lineEntities.add(le);
            }
        }
        entity.lines = lineEntities;

        if (entity.id == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        return toDomain(entity);
    }

    @Override
    public Optional<Order> findById(Long id) {
        OrderEntity entity = em.find(OrderEntity.class, id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    private Order toDomain(OrderEntity entity) {
        Order order = new Order();
        order.setId(entity.id);
        order.setCustomerId(entity.customerId);
        order.setStatus(entity.status);
        order.setTotal(entity.total);

        List<OrderLine> lines = new ArrayList<>();
        if (entity.lines != null) {
            for (OrderLineEntity le : entity.lines) {
                OrderLine line = new OrderLine();
                line.setId(le.id);
                line.setProductId(le.productId);
                line.setQuantity(le.quantity);
                line.setLinePrice(le.linePrice);
                lines.add(line);
            }
        }
        order.setLines(lines);
        return order;
    }
}
