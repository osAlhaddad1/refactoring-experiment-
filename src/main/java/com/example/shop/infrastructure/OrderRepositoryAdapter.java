package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderSpringRepository repository;

    public OrderRepositoryAdapter(OrderSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderJpaEntity entity = new OrderHeaderJpaEntity();
        entity.setId(order.getId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setTotal(order.getTotal());
        entity.setLines(order.getLines().stream().map(line -> {
            OrderLineJpaEntity le = new OrderLineJpaEntity();
            le.setId(line.getId());
            le.setProductId(line.getProductId());
            le.setQuantity(line.getQuantity());
            le.setLinePrice(line.getLinePrice());
            return le;
        }).collect(Collectors.toList()));

        OrderHeaderJpaEntity saved = repository.save(entity);

        return new OrderHeader(
                saved.getId(),
                saved.getCustomerId(),
                saved.getStatus(),
                saved.getTotal(),
                saved.getLines().stream().map(le -> new OrderLine(
                        le.getId(),
                        le.getProductId(),
                        le.getQuantity(),
                        le.getLinePrice()
                )).collect(Collectors.toList())
        );
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return repository.findById(id).map(saved -> new OrderHeader(
                saved.getId(),
                saved.getCustomerId(),
                saved.getStatus(),
                saved.getTotal(),
                saved.getLines().stream().map(le -> new OrderLine(
                        le.getId(),
                        le.getProductId(),
                        le.getQuantity(),
                        le.getLinePrice()
                )).collect(Collectors.toList())
        ));
    }
}
