package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class ShopService {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditPort auditPort;
    private final MetricsPort metricsPort;
    private final InvoiceCounterPort invoiceCounterPort;

    public ShopService(
            CategoryRepository categoryRepository,
            CustomerRepository customerRepository,
            CouponRepository couponRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            AuditPort auditPort,
            MetricsPort metricsPort,
            InvoiceCounterPort invoiceCounterPort) {
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditPort = auditPort;
        this.metricsPort = metricsPort;
        this.invoiceCounterPort = invoiceCounterPort;
    }

    @Transactional
    public Category createCategory(Category category) {
        category.setId(null);
        return categoryRepository.save(category);
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("category"));
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        customer.setId(null);
        customer.setLoyaltyPoints(0);
        return customerRepository.save(customer);
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("customer"));
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        coupon.setTimesUsed(0);
        return couponRepository.save(coupon);
    }

    @Transactional
    public Product createProduct(String name, double price, int stock, Long categoryId) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("category"));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("product"));
    }

    @Transactional
    public OrderHeader placeOrder(OrderHeader order) {
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new NotFoundException("customer"));
        if (order.getLines() == null || order.getLines().isEmpty()) {
            throw new BadRequestException("order has no lines");
        }

        double subtotal = 0;
        for (OrderLine line : order.getLines()) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new NotFoundException("product"));
            if (line.getQuantity() <= 0) {
                throw new BadRequestException("quantity must be positive");
            }
            if (product.getStock() < line.getQuantity()) {
                throw new BadRequestException("not enough stock");
            }

            double linePrice = product.getPrice() * line.getQuantity();
            if (line.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            line.setLinePrice(linePrice);
            subtotal = subtotal + linePrice;
            product.setStock(product.getStock() - line.getQuantity());
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        if (order.getCouponCode() != null && !order.getCouponCode().isEmpty()) {
            Coupon coupon = couponRepository.findByCode(order.getCouponCode())
                    .orElseThrow(() -> new BadRequestException("unknown coupon"));
            if (coupon.getTimesUsed() >= coupon.getMaxUses()) {
                throw new BadRequestException("coupon has been used up");
            }
            total = total * (100 - coupon.getPercent()) / 100;
            coupon.setTimesUsed(coupon.getTimesUsed() + 1);
            couponRepository.save(coupon);
        }

        order.setId(null);
        order.setStatus("NEW");
        order.setTotal(total);
        order.setSurcharge(0);
        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersCreated");
        auditPort.add("created order " + savedOrder.getId());
        return savedOrder;
    }

    public OrderHeader getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order"));
    }

    @Transactional
    public OrderHeader payOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!order.getStatus().equals("NEW")) {
            throw new BadRequestException("only NEW orders can be paid");
        }
        order.setStatus("PAID");
        order.setSurcharge(5.0);
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new NotFoundException("customer"));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersPaid");
        auditPort.add("paid order " + savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    public OrderHeader shipOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!order.getStatus().equals("PAID")) {
            throw new BadRequestException("only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");
        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersShipped");
        auditPort.add("shipped order " + savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    public OrderHeader cancelOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (order.getStatus().equals("SHIPPED") || order.getStatus().equals("CANCELLED")) {
            throw new BadRequestException("cannot cancel a " + order.getStatus() + " order");
        }
        for (OrderLine line : order.getLines()) {
            Product product = productRepository.findById(line.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            } 
        }
        order.setStatus("CANCELLED");
        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersCancelled");
        auditPort.add("cancelled order " + savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    public Map<String, Object> invoiceOrder(Long id) {
        OrderHeader order = getOrder(id);
        if (!order.getStatus().equals("PAID") && !order.getStatus().equals("SHIPPED")) {
            throw new BadRequestException("only paid orders can be invoiced");
        }
        long number = invoiceCounterPort.incrementAndGet();
        metricsPort.bump("invoicesIssued");
        auditPort.add("invoiced order " + order.getId());

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", number);
        invoice.put("orderId", order.getId());
        invoice.put("total", order.getTotal());
        invoice.put("surcharge", order.getSurcharge());
        invoice.put("amountDue", order.getTotal() + order.getSurcharge());
        return invoice;
    }

    public List<String> getAuditLog() {
        return auditPort.getAuditLog();
    }

    public Map<String, Integer> getMetrics() {
        return metricsPort.getMetrics();
    }
}