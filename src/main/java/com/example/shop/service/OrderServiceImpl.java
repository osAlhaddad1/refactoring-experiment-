package com.example.shop.service;

import com.example.shop.model.*;
import com.example.shop.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final AuditService auditService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository,
                            CouponRepository couponRepository,
                            AuditService auditService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.auditService = auditService;
    }

    @Override
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

        if (order.couponCode != null && !order.couponCode.isEmpty()) {
            Coupon coupon = couponRepository.findById(order.couponCode).orElse(null);
            if (coupon == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown coupon");
            }
            if (coupon.timesUsed >= coupon.maxUses) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "coupon has been used up");
            }
            total = total * (100 - coupon.percent) / 100;
            coupon.timesUsed = coupon.timesUsed + 1;
            couponRepository.save(coupon);
        }

        order.id = null;
        order.status = "NEW";
        order.total = total;
        order.surcharge = 0;
        OrderHeader savedOrder = orderRepository.save(order);

        auditService.bump("ordersCreated");
        auditService.log("created order " + savedOrder.id);

        return savedOrder;
    }

    @Override
    public OrderHeader getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }

    @Override
    @Transactional
    public OrderHeader payOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!order.status.equals("NEW")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only NEW orders can be paid");
        }
        order.status = "PAID";
        order.surcharge = 5.0;
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);

        auditService.bump("ordersPaid");
        auditService.log("paid order " + savedOrder.id);

        return savedOrder;
    }

    @Override
    @Transactional
    public OrderHeader shipOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!order.status.equals("PAID")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        OrderHeader savedOrder = orderRepository.save(order);

        auditService.bump("ordersShipped");
        auditService.log("shipped order " + savedOrder.id);

        return savedOrder;
    }

    @Override
    @Transactional
    public OrderHeader cancelOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (order.status.equals("SHIPPED") || order.status.equals("CANCELLED")) {
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

        auditService.bump("ordersCancelled");
        auditService.log("cancelled order " + savedOrder.id);

        return savedOrder;
    }

    @Override
    @Transactional
    public Map<String, Object> invoiceOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!order.status.equals("PAID") && !order.status.equals("SHIPPED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only paid orders can be invoiced");
        }
        long number = auditService.incrementInvoiceCounter();
        auditService.bump("invoicesIssued");
        auditService.log("invoiced order " + order.id);

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", number);
        invoice.put("orderId", order.id);
        invoice.put("total", order.total);
        invoice.put("surcharge", order.surcharge);
        invoice.put("amountDue", order.total + order.surcharge);
        return invoice;
    }
}