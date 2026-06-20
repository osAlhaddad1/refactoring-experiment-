package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AuditPort auditPort;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        AuditPort auditPort) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.auditPort = auditPort;
    }

    @Transactional
    public OrderInfo placeOrder(Long customerId, List<OrderLineInfo> lineInfos) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("customer"));

        if (lineInfos == null || lineInfos.isEmpty()) {
            throw new BusinessRuleException("order has no lines");
        }

        List<OrderLine> lines = lineInfos.stream()
                .map(li -> new OrderLine(null, li.getProductId(), li.getQuantity(), 0))
                .collect(Collectors.toList());

        double subtotal = 0;
        for (OrderLine line : lines) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("product"));

            if (line.getQuantity() <= 0) {
                throw new BusinessRuleException("quantity must be positive");
            }
            if (product.getStock() < line.getQuantity()) {
                throw new BusinessRuleException("not enough stock");
            }

            double linePrice = product.getPrice() * line.getQuantity();
            if (line.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            line.setLinePrice(linePrice);
            subtotal += linePrice;

            product.reserveStock(line.getQuantity());
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        Order order = new Order(null, customerId, "NEW", total, lines);
        Order saved = orderRepository.save(order);
        auditPort.log("created order " + saved.getId());

        return toInfo(saved);
    }

    public OrderInfo getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order"));
        return toInfo(order);
    }

    @Transactional
    public OrderInfo payOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order"));

        try {
            order.pay();
        } catch (IllegalStateException e) {
            throw new BusinessRuleException(e.getMessage());
        }

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("customer"));
        customer.addLoyaltyPoints((int) order.getTotal());
        customerRepository.save(customer);

        Order saved = orderRepository.save(order);
        auditPort.log("paid order " + saved.getId());
        return toInfo(saved);
    }

    @Transactional
    public OrderInfo shipOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order"));

        try {
            order.ship();
        } catch (IllegalStateException e) {
            throw new BusinessRuleException(e.getMessage());
        }

        Order saved = orderRepository.save(order);
        auditPort.log("shipped order " + saved.getId());
        return toInfo(saved);
    }

    @Transactional
    public OrderInfo cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order"));

        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BusinessRuleException("cannot cancel a " + order.getStatus() + " order");
        }

        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(product -> {
                product.restock(line.getQuantity());
                productRepository.save(product);
            });
        }

        try {
            order.cancel();
        } catch (IllegalStateException e) {
            throw new BusinessRuleException(e.getMessage());
        }

        Order saved = orderRepository.save(order);
        auditPort.log("cancelled order " + saved.getId());
        return toInfo(saved);
    }

    private OrderInfo toInfo(Order order) {
        List<OrderLineInfo> lines = order.getLines().stream()
                .map(line -> new OrderLineInfo(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderInfo(order.getId(), order.getCustomerId(), order.getStatus(), order.getTotal(), lines);
    }
}