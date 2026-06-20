package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
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
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.id = order.id;
        entity.customerId = order.customerId;
        entity.status = order.status;
        entity.total = order.total;
        entity.surcharge = order.surcharge;
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
        OrderHeaderEntity saved = repository.save(entity);
        order.id = saved.id;
        if (saved.lines != null && order.lines != null) {
            for (int i = 0; i < saved.lines.size(); i++) {
                order.lines.get(i).id = saved.lines.get(i).id;
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
                for (OrderLineEntity lineEntity : entity.lines) {
                    OrderLine line = new OrderLine();
                    line.id = lineEntity.id;
                    line.productId = lineEntity.productId;
                    line.quantity = lineEntity.quantity;
                    line.linePrice = lineEntity.linePrice;
                    order.lines.add(line);
                }
            }
            return order;
        });
    }
}
