package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderAdapter implements OrderRepository {
    private final SpringDataOrderRepository repository;

    public OrderAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.id = order.id;
        entity.customerId = order.customerId;
        entity.status = order.status;
        entity.total = order.total;
        entity.surcharge = order.surcharge;
        if (order.lines != null) {
            entity.lines = order.lines.stream().map(line -> {
                OrderLineEntity lineEntity = new OrderLineEntity();
                lineEntity.id = line.id;
                lineEntity.productId = line.productId;
                lineEntity.quantity = line.quantity;
                lineEntity.linePrice = line.linePrice;
                return lineEntity;
            }).collect(Collectors.toList());
        }
        entity = repository.save(entity);
        order.id = entity.id;
        if (entity.lines != null) {
            for (int i = 0; i < entity.lines.size(); i++) {
                order.lines.get(i).id = entity.lines.get(i).id;
            }
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
            order.surcharge = entity.surcharge;
            if (entity.lines != null) {
                order.lines = entity.lines.stream().map(lineEntity -> {
                    OrderLine line = new OrderLine();
                    line.id = lineEntity.id;
                    line.productId = lineEntity.productId;
                    line.quantity = lineEntity.quantity;
                    line.linePrice = lineEntity.linePrice;
                    return line;
                }).collect(Collectors.toList());
            }
            return order;
        });
    }
}
