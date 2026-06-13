package com.example.shop.infrastructure;

import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
interface SpringDataOrderRepository extends JpaRepository<OrderHeaderJpaEntity, Long> {}

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository springRepository;

    public OrderRepositoryAdapter(SpringDataOrderRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public OrderHeader save(OrderHeader order) {
        OrderHeaderJpaEntity jpaEntity = new OrderHeaderJpaEntity(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotal(),
                order.getLines().stream()
                        .map(line -> new OrderLineJpaEntity(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                        .collect(Collectors.toList())
        );
        OrderHeaderJpaEntity saved = springRepository.save(jpaEntity);
        order.setId(saved.getId());
        for (int i = 0; i < saved.getLines().size(); i++) {
            order.getLines().get(i).setId(saved.getLines().get(i).getId());
        }
        return order;
    }

    @Override
    public Optional<OrderHeader> findById(Long id) {
        return springRepository.findById(id)
                .map(jpa -> new OrderHeader(
                        jpa.getId(),
                        jpa.getCustomerId(),
                        jpa.getStatus(),
                        jpa.getTotal(),
                        jpa.getLines().stream()
                                .map(line -> new OrderLine(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                                .collect(Collectors.toList())
                ));
    }
}