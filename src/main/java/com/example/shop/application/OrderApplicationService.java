package com.example.shop.application;

import com.example.shop.domain.*;
import com.example.shop.domain.exception.BadRequestException;
import com.example.shop.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class OrderApplicationService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditPort auditPort;

    public OrderApplicationService(CustomerRepository customerRepository,
                                   ProductRepository productRepository,
                                   OrderRepository orderRepository,
                                   AuditPort auditPort) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditPort = auditPort;
    }

    public OrderHeader placeOrder(Long customerId, List<OrderLine> lines) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("customer not found"));

        if (lines == null || lines.isEmpty()) {
            throw new BadRequestException("order has no lines");
        }

        double subtotal = 0;
        for (OrderLine line : lines) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new NotFoundException("product not found"));

            product.reserveStock(line.getQuantity());
            productRepository.save(product);

            double linePrice = product.getPrice() * line.getQuantity();
            if (line.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            line.setLinePrice(linePrice);
            subtotal += linePrice;
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        OrderHeader order = new OrderHeader(null, customer.getId(), "NEW", total, lines);
        OrderHeader savedOrder = orderRepository.save(order);
        auditPort.log("created order " + savedOrder.getId());
        return savedOrder;
    }

    public OrderHeader getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
    }

    public OrderHeader payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));

        order.pay();

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new NotFoundException("customer not found"));

        customer.addLoyaltyPoints((int) order.getTotal());
        customerRepository.save(customer);

        OrderHeader savedOrder = orderRepository.save(order);
        auditPort.log("paid order " + savedOrder.getId());
        return savedOrder;
    }

    public OrderHeader shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));

        order.ship();

        OrderHeader savedOrder = orderRepository.save(order);
        auditPort.log("shipped order " + savedOrder.getId());
        return savedOrder;
    }

    public OrderHeader cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));

        order.cancel();

        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(product -> {
                product.restock(line.getQuantity());
                productRepository.save(product);
            });
        }

        OrderHeader savedOrder = orderRepository.save(order);
        auditPort.log("cancelled order " + savedOrder.getId());
        return savedOrder;
    }
}