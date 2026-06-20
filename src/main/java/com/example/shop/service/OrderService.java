package com.example.shop.service;

import com.example.shop.domain.Customer;
import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.Product;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        AuditService auditService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    @Transactional
    public OrderHeader placeOrder(OrderHeader order) {
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));

        if (order.lines == null || order.lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order has no lines");
        }

        double subtotal = 0;
        for (OrderLine line : order.lines) {
            Product product = productRepository.findById(line.productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));

            if (line.quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
            }
            if (product.stock < line.quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
            }

            double linePrice = product.price * line.quantity;
            if (line.quantity >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            line.linePrice = linePrice;
            subtotal = subtotal + linePrice;

            product.stock = product.stock - line.quantity;
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        order.id = null;
        order.status = "NEW";
        order.total = total;
        OrderHeader savedOrder = orderRepository.save(order);
        auditService.log("created order " + savedOrder.id);
        return savedOrder;
    }

    public OrderHeader getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }

    @Transactional
    public OrderHeader payOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!"NEW".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only NEW orders can be paid");
        }
        order.status = "PAID";
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);
        auditService.log("paid order " + savedOrder.id);
        return savedOrder;
    }

    @Transactional
    public OrderHeader shipOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!"PAID".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        OrderHeader savedOrder = orderRepository.save(order);
        auditService.log("shipped order " + savedOrder.id);
        return savedOrder;
    }

    @Transactional
    public OrderHeader cancelOrder(Long id) {
        OrderHeader order = getOrder(id);
        if ("SHIPPED".equals(order.status) || "CANCELLED".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot cancel a " + order.status + " order");
        }
        for (OrderLine line : order.lines) {
            Product product = productRepository.findById(line.productId).orElse(null);
            if (product != null) {
                product.stock = product.stock + line.quantity;
                productRepository.save(product);
            }
        }
        order.status = "CANCELLED";
        OrderHeader savedOrder = orderRepository.save(order);
        auditService.log("cancelled order " + savedOrder.id);
        return savedOrder;
    }
}