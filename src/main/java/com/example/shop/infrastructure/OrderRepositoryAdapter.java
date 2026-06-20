package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            List<OrderLineJpaEntity> lineEntities = new ArrayList<>();
            for (OrderLine line : order.getLines()) {
                OrderLineJpaEntity lineEntity = new OrderLineJpaEntity();
                lineEntity.id = line.getId();
                lineEntity.productId = line.getProductId();
                lineEntity.quantity = line.getQuantity();
                lineEntity.linePrice = line.getLinePrice();
                lineEntities.add(lineEntity);
            }
            entity.lines = lineEntities;
        }
        
        entity = repository.save(entity);
        
        order.setId(entity.id);
        order.setCustomerId(entity.customerId);
        order.setStatus(entity.status);
        order.setTotal(entity.total);
        
        if (entity.lines != null) {
            List<OrderLine> lines = new ArrayList<>();
            for (OrderLineJpaEntity lineEntity : entity.lines) {
                OrderLine line = new OrderLine();
                line.setId(lineEntity.id);
                line.setProductId(lineEntity.productId);
                line.setQuantity(lineEntity.quantity);
                line.setLinePrice(lineEntity.linePrice);
                lines.add(line);
            }
            order.setLines(lines);
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
                List<OrderLine> lines = new ArrayList<>();
                for (OrderLineJpaEntity lineEntity : entity.lines) {
                    OrderLine line = new OrderLine();
                    line.setId(lineEntity.id);
                    line.setProductId(lineEntity.productId);
                    line.setQuantity(lineEntity.quantity);
                    line.setLinePrice(lineEntity.linePrice);
                    lines.add(line);
                }
                order.setLines(lines);
            }
            return order;
        });
    }
}
