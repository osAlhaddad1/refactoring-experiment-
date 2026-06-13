package com.example.shop.infrastructure;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository springDataOrderRepository;

    public OrderRepositoryAdapter(SpringDataOrderRepository springDataOrderRepository) {
        this.springDataOrderRepository = springDataOrderRepository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotal()
        );
        OrderJpaEntity saved = springDataOrderRepository.save(entity);
        return new Order(saved.getId(), saved.getProductId(), saved.getQuantity(), saved.getTotal());
    }

    @Override
    public Optional<Order> findById(Long id) {
        return springDataOrderRepository.findById(id)
                .map(entity -> new Order(entity.getId(), entity.getProductId(), entity.getQuantity(), entity.getTotal()));
    }
}