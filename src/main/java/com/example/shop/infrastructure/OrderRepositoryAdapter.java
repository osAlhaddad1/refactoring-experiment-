package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository repository;

    public OrderRepositoryAdapter(OrderJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderJpaEntity entity = new OrderHeaderJpaEntity();
        entity.id = order.getId();
        entity.customerId = order.getCustomerId();
        entity.status = order.getStatus();
        entity.total = order.getTotal();
        if (order.getLines() != null) {
            entity.lines = order.getLines().stream().map(line -> {
                OrderLineJpaEntity lineEntity = new OrderLineJpaEntity();
                lineEntity.id = line.getId();
                lineEntity.productId = line.getProductId();
                lineEntity.quantity = line.getQuantity();
                lineEntity.linePrice = line.getLinePrice();
                return lineEntity;
            }).collect(Collectors.toList());
        }
        entity = repository.save(entity);
        order.setId(entity.id);
        order.setStatus(entity.status);
        order.setTotal(entity.total);
        if (entity.lines != null) {
            for (int i = 0; i < entity.lines.size(); i++) {
                order.getLines().get(i).setId(entity.lines.get(i).id);
            }
        }
        return order;
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return repository.findById(id).map(entity -> {
            OrderHeader order = new OrderHeader();
            order.setId(entity.id);
            order.setCustomerId(entity.customerId);
            order.setStatus(entity.status);
            order.setTotal(entity.total);
            if (entity.lines != null) {
                order.setLines(entity.lines.stream().map(lineEntity -> {
                    OrderLine line = new OrderLine();
                    line.setId(lineEntity.id);
                    line.setProductId(lineEntity.productId);
                    line.setQuantity(lineEntity.quantity);
                    line.setLinePrice(lineEntity.linePrice);
                    return line;
                }).collect(Collectors.toList()));
            }
            return order;
        });
    }
}