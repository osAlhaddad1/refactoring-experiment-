package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    private final OrderJpaRepository repository;

    public OrderRepositoryAdapter(OrderJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.id = order.id;
        entity.customerId = order.customerId;
        entity.status = order.status;
        entity.total = order.total;
        if (order.lines != null) {
            entity.lines = order.lines.stream().map(line -> {
                OrderLineEntity le = new OrderLineEntity();
                le.id = line.id;
                le.productId = line.productId;
                le.quantity = line.quantity;
                le.linePrice = line.linePrice;
                return le;
            }).collect(Collectors.toList());
        }
        
        OrderHeaderEntity saved = repository.save(entity);
        
        order.id = saved.id;
        order.customerId = saved.customerId;
        order.status = saved.status;
        order.total = saved.total;
        if (saved.lines != null) {
            order.lines = saved.lines.stream().map(le -> {
                OrderLine line = new OrderLine();
                line.id = le.id;
                line.productId = le.productId;
                line.quantity = le.quantity;
                line.linePrice = le.linePrice;
                return line;
            }).collect(Collectors.toList());
        }
        return order;
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return repository.findById(id).map(entity -> {
            OrderHeader order = new OrderHeader();
            order.id = entity.id;
            order.customerId = entity.customerId;
            order.status = entity.status;
            order.total = entity.total;
            if (entity.lines != null) {
                order.lines = entity.lines.stream().map(le -> {
                    OrderLine line = new OrderLine();
                    line.id = le.id;
                    line.productId = le.productId;
                    line.quantity = le.quantity;
                    line.linePrice = le.linePrice;
                    return line;
                }).collect(Collectors.toList());
            }
            return order;
        });
    }
}