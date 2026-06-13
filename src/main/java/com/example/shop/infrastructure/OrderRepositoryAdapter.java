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
        entity.setId(order.getId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setTotal(order.getTotal());
        entity.setLines(order.getLines().stream().map(line -> {
            OrderLineEntity le = new OrderLineEntity();
            le.setId(line.getId());
            le.setProductId(line.getProductId());
            le.setQuantity(line.getQuantity());
            le.setLinePrice(line.getLinePrice());
            return le;
        }).collect(Collectors.toList()));

        OrderHeaderEntity saved = repository.save(entity);
        
        OrderHeader result = new OrderHeader();
        result.setId(saved.getId());
        result.setCustomerId(saved.getCustomerId());
        result.setStatus(saved.getStatus());
        result.setTotal(saved.getTotal());
        result.setLines(saved.getLines().stream().map(le -> new OrderLine(
            le.getId(),
            le.getProductId(),
            le.getQuantity(),
            le.getLinePrice()
        )).collect(Collectors.toList()));
        return result;
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return repository.findById(id).map(entity -> {
            OrderHeader order = new OrderHeader();
            order.setId(entity.getId());
            order.setCustomerId(entity.getCustomerId());
            order.setStatus(entity.getStatus());
            order.setTotal(entity.getTotal());
            order.setLines(entity.getLines().stream().map(le -> new OrderLine(
                le.getId(),
                le.getProductId(),
                le.getQuantity(),
                le.getLinePrice()
            )).collect(Collectors.toList()));
            return order;
        });
    }
}