package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.setId(order.getId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setTotal(order.getTotal());
        entity.setSurcharge(order.getSurcharge());
        
        List<OrderLineEntity> lineEntities = new ArrayList<>();
        for (OrderLine line : order.getLines()) {
            OrderLineEntity le = new OrderLineEntity();
            le.setId(line.getId());
            le.setProductId(line.getProductId());
            le.setQuantity(line.getQuantity());
            le.setLinePrice(line.getLinePrice());
            lineEntities.add(le);
        }
        entity.setLines(lineEntities);

        OrderHeaderEntity saved = repository.save(entity);

        List<OrderLine> lines = new ArrayList<>();
        for (OrderLineEntity le : saved.getLines()) {
            lines.add(new OrderLine(le.getId(), le.getProductId(), le.getQuantity(), le.getLinePrice()));
        }

        return new OrderHeader(
            saved.getId(),
            saved.getCustomerId(),
            saved.getStatus(),
            saved.getTotal(),
            saved.getSurcharge(),
            order.getCouponCode(),
            lines
        );
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return repository.findById(id).map(entity -> {
            List<OrderLine> lines = new ArrayList<>();
            for (OrderLineEntity le : entity.getLines()) {
                lines.add(new OrderLine(le.getId(), le.getProductId(), le.getQuantity(), le.getLinePrice()));
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
        });
    }
}
