package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ShopService {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditPort auditPort;
    private final MetricsPort metricsPort;
    private final InvoicePort invoicePort;
    private final OrderPricingEngine orderPricingEngine = new OrderPricingEngine();

    public ShopService(CategoryRepository categoryRepository,
                       CustomerRepository customerRepository,
                       CouponRepository couponRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       AuditPort auditPort,
                       MetricsPort metricsPort,
                       InvoicePort invoicePort) {
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditPort = auditPort;
        this.metricsPort = metricsPort;
        this.invoicePort = invoicePort;
    }

    public Category createCategory(Category category) {
        category.id = null;
        return categoryRepository.save(category);
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("category"));
    }

    public Customer createCustomer(Customer customer) {
        customer.id = null;
        customer.loyaltyPoints = 0;
        return customerRepository.save(customer);
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("customer"));
    }

    public Coupon createCoupon(Coupon coupon) {
        coupon.timesUsed = 0;
        return couponRepository.save(coupon);
    }

    public Product createProduct(String name, double price, int stock, Long categoryId) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("category"));
        }
        Product product = new Product(null, name, price, stock, category);
        return productRepository.save(product);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("product"));
    }

    public OrderHeader placeOrder(OrderHeader order) {
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> new EntityNotFoundException("customer"));

        if (order.lines == null || order.lines.isEmpty()) {
            throw new DomainException("order has no lines");
        }

        List<Product> products = new ArrayList<>();
        for (OrderLine line : order.lines) {
            Product product = productRepository.findById(line.productId)
                    .orElseThrow(() -> new EntityNotFoundException("product"));
            products.add(product);
        }

        Coupon coupon = null;
        if (order.couponCode != null && !order.couponCode.isEmpty()) {
            coupon = couponRepository.findByCode(order.couponCode)
                    .orElseThrow(() -> new DomainException("unknown coupon"));
        }

        orderPricingEngine.calculatePriceAndReduceStock(order, products, coupon);

        for (Product product : products) {
            productRepository.save(product);
        }
        if (coupon != null) {
            couponRepository.save(coupon);
        }

        order.id = null;
        order.status = "NEW";
        order.surcharge = 0;
        OrderHeader savedOrder = orderRepository.save(order);

        metricsPort.bump("ordersCreated");
        auditPort.log("created order " + savedOrder.id);

        return savedOrder;
    }

    public OrderHeader getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order"));
    }

    public OrderHeader payOrder(Long id) {
        OrderHeader order = getOrder(id);
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> new EntityNotFoundException("customer"));

        orderPricingEngine.pay(order, customer);

        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);

        metricsPort.bump("ordersPaid");
        auditPort.log("paid order " + savedOrder.id);

        return savedOrder;
    }

    public OrderHeader shipOrder(Long id) {
        OrderHeader order = getOrder(id);

        orderPricingEngine.ship(order);

        OrderHeader savedOrder = orderRepository.save(order);

        metricsPort.bump("ordersShipped");
        auditPort.log("shipped order " + savedOrder.id);

        return savedOrder;
    }

    public OrderHeader cancelOrder(Long id) {
        OrderHeader order = getOrder(id);

        List<Product> products = new ArrayList<>();
        for (OrderLine line : order.lines) {
            productRepository.findById(line.productId).ifPresent(products::add);
        }

        orderPricingEngine.cancel(order, products);

        for (Product product : products) {
            productRepository.save(product);
        }
        OrderHeader savedOrder = orderRepository.save(order);

        metricsPort.bump("ordersCancelled");
        auditPort.log("cancelled order " + savedOrder.id);

        return savedOrder;
    }

    public Map<String, Object> invoiceOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!"PAID".equals(order.status) && !"SHIPPED".equals(order.status)) {
            throw new DomainException("only paid orders can be invoiced");
        }

        long number = invoicePort.nextInvoiceNumber();
        metricsPort.bump("invoicesIssued");
        auditPort.log("invoiced order " + order.id);

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", number);
        invoice.put("orderId", order.id);
        invoice.put("total", order.total);
        invoice.put("surcharge", order.surcharge);
        invoice.put("amountDue", order.total + order.surcharge);
        return invoice;
    }

    public List<String> getAuditLogs() {
        return auditPort.getLogs();
    }

    public Map<String, Integer> getMetrics() {
        return metricsPort.getMetrics();
    }
}
