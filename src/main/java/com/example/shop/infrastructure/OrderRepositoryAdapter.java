package com.example.shop.infrastructure;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderRecord save(OrderRecord order) {
        OrderJpaEntity entity = new OrderJpaEntity(order.getId(), order.getProductId(), order.getQuantity(), order.getTotal());
        OrderJpaEntity saved = repository.save(entity);
        order.setId(saved.getId());
        return order;
    }

    @Override
    public Optional<OrderRecord> findById(Long id) {
        return repository.findById(id)
                .map(entity -> new OrderRecord(entity.getId(), entity.getProductId(), entity.getQuantity(), entity.getTotal()));
    }
}
