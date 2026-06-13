package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AuditLog auditLog;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        AuditLog auditLog) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.auditLog = auditLog;
    }

    @Transactional
    public OrderDto placeOrder(OrderDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("customer not found"));

        if (dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new ValidationException("order has no lines");
        }

        double subtotal = 0;
        List<OrderLine> domainLines = new ArrayList<>();

        for (OrderLineDto lineDto : dto.getLines()) {
            Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("product not found"));

            if (lineDto.getQuantity() <= 0) {
                throw new ValidationException("quantity must be positive");
            }
            if (product.getStock() < lineDto.getQuantity()) {
                throw new ValidationException("not enough stock");
            }

            double linePrice = product.getPrice() * lineDto.getQuantity();
            if (lineDto.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }

            subtotal += linePrice;

            // reserve stock
            product.setStock(product.getStock() - lineDto.getQuantity());
            productRepository.save(product);

            OrderLine line = new OrderLine();
            line.setProductId(product.getId());
            line.setQuantity(lineDto.getQuantity());
            line.setLinePrice(linePrice);
            domainLines.add(line);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        OrderHeader order = new OrderHeader();
        order.setId(null);
        order.setCustomerId(customer.getId());
        order.setStatus("NEW");
        order.setTotal(total);
        order.setLines(domainLines);

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("created order " + saved.getId());

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));
        return mapToDto(order);
    }

    @Transactional
    public OrderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));

        if (!"NEW".equals(order.getStatus())) {
            throw new ValidationException("only NEW orders can be paid");
        }

        order.setStatus("PAID");

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("customer not found"));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
        customerRepository.save(customer);

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("paid order " + saved.getId());

        return mapToDto(saved);
    }

    @Transactional
    public OrderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));

        if (!"PAID".equals(order.getStatus())) {
            throw new ValidationException("only PAID orders can be shipped");
        }

        order.setStatus("SHIPPED");
        OrderHeader saved = orderRepository.save(order);
        auditLog.log("shipped order " + saved.getId());

        return mapToDto(saved);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));

        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new ValidationException("cannot cancel a " + order.getStatus() + " order");
        }

        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            });
        }

        order.setStatus("CANCELLED");
        OrderHeader saved = orderRepository.save(order);
        auditLog.log("cancelled order " + saved.getId());

        return mapToDto(saved);
    }

    private OrderDto mapToDto(OrderHeader order) {
        List<OrderLineDto> lineDtos = order.getLines().stream()
                .map(line -> new OrderLineDto(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderDto(order.getId(), order.getCustomerId(), order.getStatus(), order.getTotal(), lineDtos);
    }
}
