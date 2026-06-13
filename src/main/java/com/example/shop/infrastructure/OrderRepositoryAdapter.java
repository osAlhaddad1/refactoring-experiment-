package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringOrderRepository springRepository;

    public OrderRepositoryAdapter(SpringOrderRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.id = order.id;
        entity.customerId = order.customerId;
        entity.status = order.status;
        entity.total = order.total;
        entity.surcharge = order.surcharge;
        entity.lines = new ArrayList<>();
        if (order.lines != null) {
            for (OrderLine line : order.lines) {
                OrderLineEntity lineEntity = new OrderLineEntity();
                lineEntity.id = line.id;
                lineEntity.productId = line.productId;
                lineEntity.quantity = line.quantity;
                lineEntity.linePrice = line.linePrice;
                entity.lines.add(lineEntity);
            }
        }
        OrderHeaderEntity saved = springRepository.save(entity);
        List<OrderLine> savedLines = new ArrayList<>();
        if (saved.lines != null) {
            for (OrderLineEntity lineEntity : saved.lines) {
                savedLines.add(new OrderLine(lineEntity.id, lineEntity.productId, lineEntity.quantity, lineEntity.linePrice));
            }
        }
        return new OrderHeader(saved.id, saved.customerId, saved.status, saved.total, saved.surcharge, order.couponCode, savedLines);
    }

    @Override
    public OrderHeader findById(Long id) {
        return springRepository.findById(id)
                .map(entity -> {
                    List<OrderLine> lines = new ArrayList<>();
                    if (entity.lines != null) {
                        for (OrderLineEntity lineEntity : entity.lines) {
                            lines.add(new OrderLine(lineEntity.id, lineEntity.productId, lineEntity.quantity, lineEntity.linePrice));
                        }
                    }
                    return new OrderHeader(entity.id, entity.customerId, entity.status, entity.total, entity.surcharge, null, lines);
                })
                .orElse(null);
    }
}