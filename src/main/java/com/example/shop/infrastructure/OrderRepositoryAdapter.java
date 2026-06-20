package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
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
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity = EntityMapper.toEntity(order);
        OrderHeaderEntity saved = repository.save(entity);
        OrderHeader domain = EntityMapper.toDomain(saved);
        if (domain != null) {
            domain.couponCode = order.couponCode;
        }
        return domain;
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return repository.findById(id).map(EntityMapper::toDomain);
    }
}
