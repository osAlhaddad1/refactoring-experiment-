package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaRepository;

    public OrderRepositoryAdapter(JpaOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity = toEntity(order);
        OrderHeaderEntity saved = jpaRepository.save(entity);
        OrderHeader domain = toDomain(saved);
        domain.setCouponCode(order.getCouponCode());
        return domain;
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private OrderHeaderEntity toEntity(OrderHeader domain) {
        if (domain == null) return null;
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.setId(domain.getId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setStatus(domain.getStatus());
        entity.setTotal(domain.getTotal());
        entity.setSurcharge(domain.getSurcharge());
        if (domain.getLines() != null) {
            List<OrderLineEntity> lineEntities = domain.getLines().stream()
                    .map(line -> {
                        OrderLineEntity le = new OrderLineEntity();
                        le.setId(line.getId());
                        le.setProductId(line.getProductId());
                        le.setQuantity(line.getQuantity());
                        le.setLinePrice(line.getLinePrice());
                        return le;
                    }).collect(Collectors.toList());
            entity.setLines(lineEntities);
        }
        return entity;
    }

    private OrderHeader toDomain(OrderHeaderEntity entity) {
        if (entity == null) return null;
        List<OrderLine> lines = new ArrayList<>();
        if (entity.getLines() != null) {
            lines = entity.getLines().stream()
                    .map(le -> new OrderLine(le.getId(), le.getProductId(), le.getQuantity(), le.getLinePrice()))
                    .collect(Collectors.toList());
        }
        return new OrderHeader(
                entity.getId(),
                entity.getCustomerId(),
                entity.getStatus(),
                entity.getTotal(),
                entity.getSurcharge(),
                null,
                lines
        );
    }
}