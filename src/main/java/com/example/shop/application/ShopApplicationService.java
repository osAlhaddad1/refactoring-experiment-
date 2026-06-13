package com.example.shop.application;

import com.example.shop.domain.*;
import java.util.List;
import java.util.Optional;

public class ShopApplicationService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditPort auditPort;

    public ShopApplicationService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            AuditPort auditPort) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditPort = auditPort;
    }

    public Customer createCustomer(Customer customer) {
        customer.setId(null);
        customer.setLoyaltyPoints(0);
        return customerRepository.save(customer);
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("customer"));
    }

    public Product createProduct(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("product"));
    }

    public Order placeOrder(Order order) {
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("customer"));

        if (order.getLines() == null || order.getLines().isEmpty()) {
            throw new InvalidOrderException("order has no lines");
        }

        double subtotal = 0;
        for (OrderLine line : order.getLines()) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("product"));

            if (line.getQuantity() <= 0) {
                throw new InvalidOrderException("quantity must be positive");
            }
            if (product.getStock() < line.getQuantity()) {
                throw new InvalidOrderException("not enough stock");
            }

            double linePrice = product.getPrice() * line.getQuantity();
            if (line.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            line.setLinePrice(linePrice);
            subtotal += linePrice;

            product.setStock(product.getStock() - line.getQuantity());
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        order.setId(null);
        order.setStatus("NEW");
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);
        auditPort.log("created order " + savedOrder.getId());
        return savedOrder;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order"));
    }

    public Order payOrder(Long id) {
        Order order = getOrder(id);
        if (!"NEW".equals(order.getStatus())) {
            throw new InvalidOrderException("only NEW orders can be paid");
        }
        order.setStatus("PAID");

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("customer"));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
        customerRepository.save(customer);

        Order savedOrder = orderRepository.save(order);
        auditPort.log("paid order " + savedOrder.getId());
        return savedOrder;
    }

    public Order shipOrder(Long id) {
        Order order = getOrder(id);
        if (!"PAID".equals(order.getStatus())) {
            throw new InvalidOrderException("only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");

        Order savedOrder = orderRepository.save(order);
        auditPort.log("shipped order " + savedOrder.getId());
        return savedOrder;
    }

    public Order cancelOrder(Long id) {
        Order order = getOrder(id);
        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new InvalidOrderException("cannot cancel a " + order.getStatus() + " order");
        }

        for (OrderLine line : order.getLines()) {
            Optional<Product> productOpt = productRepository.findById(line.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus("CANCELLED");
        Order savedOrder = orderRepository.save(order);
        auditPort.log("cancelled order " + savedOrder.getId());
        return savedOrder;
    }

    public List<String> getAuditLogs() {
        return auditPort.getLogs();
    }
}
