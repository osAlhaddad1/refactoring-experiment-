package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity;
        if (order.getId() != null) {
            entity = repository.findById(order.getId()).orElseGet(OrderEntity::new);
        } else {
            entity = new OrderEntity();
        }
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setTotal(order.getTotal());
        entity.setSurcharge(order.getSurcharge());
        
        entity.getLines().clear();
        if (order.getLines() != null) {
            for (OrderLine line : order.getLines()) {
                OrderLineEntity le = new OrderLineEntity();
                le.setId(line.getId());
                le.setProductId(line.getProductId());
                le.setQuantity(line.getQuantity());
                le.setLinePrice(line.getLinePrice());
                entity.getLines().add(le);
            }
        }

        OrderEntity saved = repository.save(entity);
        order.setId(saved.getId());
        
        for (int i = 0; i < saved.getLines().size(); i++) {
            order.getLines().get(i).setId(saved.getLines().get(i).getId());
        }
        
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return repository.findById(id).map(entity -> {
            List<OrderLine> lines = new ArrayList<>();
            for (OrderLineEntity le : entity.getLines()) {
                lines.add(new OrderLine(le.getId(), le.getProductId(), le.getQuantity(), le.getLinePrice()));
            }
            return new Order(entity.getId(), entity.getCustomerId(), entity.getStatus(), entity.getTotal(), entity.getSurcharge(), null, lines);
        });
    }
}
