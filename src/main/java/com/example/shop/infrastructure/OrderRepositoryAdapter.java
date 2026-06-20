package com.example.shop.infrastructure;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderRecord save(OrderRecord order) {
        OrderJpaEntity entity = toEntity(order);
        OrderJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OrderRecord> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    private OrderJpaEntity toEntity(OrderRecord domain) {
        if (domain == null) return null;
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(domain.getId());
        entity.setProductId(domain.getProductId());
        entity.setQuantity(domain.getQuantity());
        entity.setTotal(domain.getTotal());
        return entity;
    }

    private OrderRecord toDomain(OrderJpaEntity entity) {
        if (entity == null) return null;
        return new OrderRecord(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getTotal()
        );
    }
}
