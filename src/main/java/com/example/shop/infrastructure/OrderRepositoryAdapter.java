package com.example.shop.infrastructure;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.OrderRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderRecord save(OrderRecord order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotal()
        );
        OrderJpaEntity saved = jpaRepository.save(entity);
        return new OrderRecord(
                saved.getId(),
                saved.getProductId(),
                saved.getQuantity(),
                saved.getTotal()
        );
    }

    @Override
    public Optional<OrderRecord> findById(Long id) {
        return jpaRepository.findById(id)
                .map(entity -> new OrderRecord(
                        entity.getId(),
                        entity.getProductId(),
                        entity.getQuantity(),
                        entity.getTotal()
                ));
    }
}
