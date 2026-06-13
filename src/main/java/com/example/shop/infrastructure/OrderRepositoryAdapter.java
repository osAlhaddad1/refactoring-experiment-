package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.id = order.getId();
        entity.customerId = order.getCustomerId();
        entity.status = order.getStatus();
        entity.total = order.getTotal();
        if (order.getLines() != null) {
            entity.lines = order.getLines().stream().map(line -> {
                OrderLineEntity le = new OrderLineEntity();
                le.id = line.getId();
                le.productId = line.getProductId();
                le.quantity = line.getQuantity();
                le.linePrice = line.getLinePrice();
                return le;
            }).collect(Collectors.toList());
        }

        OrderHeaderEntity saved = repository.save(entity);

        order.setId(saved.id);
        order.setCustomerId(saved.customerId);
        order.setStatus(saved.status);
        order.setTotal(saved.total);
        if (saved.lines != null) {
            order.setLines(saved.lines.stream().map(le -> {
                OrderLine line = new OrderLine();
                line.setId(le.id);
                line.setProductId(le.productId);
                line.setQuantity(le.quantity);
                line.setLinePrice(le.linePrice);
                return line;
            }).collect(Collectors.toList()));
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
                order.setLines(entity.lines.stream().map(le -> {
                    OrderLine line = new OrderLine();
                    line.setId(le.id);
                    line.setProductId(le.productId);
                    line.setQuantity(le.quantity);
                    line.setLinePrice(le.linePrice);
                    return line;
                }).collect(Collectors.toList()));
            }
            return order;
        });
    }
}
