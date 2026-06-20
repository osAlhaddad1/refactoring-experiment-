package com.example.shop.service;

import com.example.shop.model.Customer;
import com.example.shop.model.OrderHeader;
import com.example.shop.model.OrderLine;
import com.example.shop.model.Product;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ShopService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditPort auditPort;

    public ShopService(CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       AuditPort auditPort) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditPort = auditPort;
    }

    public Customer createCustomer(Customer customer) {
        customer.id = null;
        customer.loyaltyPoints = 0;
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> notFound("customer"));
    }

    public Product createProduct(Product product) {
        product.id = null;
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> notFound("product"));
    }

    public OrderHeader placeOrder(OrderHeader order) {
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> notFound("customer"));
        if (order.lines == null || order.lines.isEmpty()) {
            throw badRequest("order has no lines");
        }

        double subtotal = 0;
        for (OrderLine line : order.lines) {
            Product product = productRepository.findById(line.productId)
                    .orElseThrow(() -> notFound("product"));
            if (line.quantity <= 0) {
                throw badRequest("quantity must be positive");
            }
            if (product.stock < line.quantity) {
                throw badRequest("not enough stock");
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
        auditPort.log("created order " + savedOrder.id);
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public OrderHeader getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
    }

    public OrderHeader payOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!order.status.equals("NEW")) {
            throw badRequest("only NEW orders can be paid");
        }
        order.status = "PAID";
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> notFound("customer"));
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);
        auditPort.log("paid order " + savedOrder.id);
        return savedOrder;
    }

    public OrderHeader shipOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!order.status.equals("PAID")) {
            throw badRequest("only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        OrderHeader savedOrder = orderRepository.save(order);
        auditPort.log("shipped order " + savedOrder.id);
        return savedOrder;
    }

    public OrderHeader cancelOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (order.status.equals("SHIPPED") || order.status.equals("CANCELLED")) {
            throw badRequest("cannot cancel a " + order.status + " order");
        }
        for (OrderLine line : order.lines) {
            productRepository.findById(line.productId).ifPresent(product -> {
                product.stock = product.stock + line.quantity;
                productRepository.save(product);
            });
        }
        order.status = "CANCELLED";
        OrderHeader savedOrder = orderRepository.save(order);
        auditPort.log("cancelled order " + savedOrder.id);
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<String> getAuditLogs() {
        return auditPort.getLogs();
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}