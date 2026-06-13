package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = toEntity(order);
        OrderJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    private OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setTotal(order.getTotal());
        if (order.getLines() != null) {
            entity.setLines(order.getLines().stream().map(this::toEntity).collect(Collectors.toList()));
        }
        return entity;
    }

    private OrderLineJpaEntity toEntity(OrderLine line) {
        OrderLineJpaEntity entity = new OrderLineJpaEntity();
        entity.setId(line.getId());
        entity.setProductId(line.getProductId());
        entity.setQuantity(line.getQuantity());
        entity.setLinePrice(line.getLinePrice());
        return entity;
    }

    private Order toDomain(OrderJpaEntity entity) {
        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getStatus(),
                entity.getTotal(),
                entity.getLines().stream().map(this::toDomain).collect(Collectors.toList())
        );
    }

    private OrderLine toDomain(OrderLineJpaEntity entity) {
        return new OrderLine(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getLinePrice()
        );
    }
}
