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
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = DtoMapper.toDomain(dto);
        category.id = null;
        Category saved = categoryRepository.save(category);
        return DtoMapper.toDto(saved);
    }

    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("category not found"));
        return DtoMapper.toDto(category);
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = DtoMapper.toDomain(dto);
        customer.id = null;
        customer.loyaltyPoints = 0;
        Customer saved = customerRepository.save(customer);
        return DtoMapper.toDto(saved);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("customer not found"));
        return DtoMapper.toDto(customer);
    }

    @Transactional
    public CouponDto createCoupon(CouponDto dto) {
        Coupon coupon = DtoMapper.toDomain(dto);
        coupon.timesUsed = 0;
        Coupon saved = couponRepository.save(coupon);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public ProductDto createProduct(String name, double price, int stock, Long categoryId) {
        Product product = new Product();
        product.name = name;
        product.price = price;
        product.stock = stock;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("category not found"));
            product.category = category;
        }
        Product saved = productRepository.save(product);
        return DtoMapper.toDto(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("product not found"));
        return DtoMapper.toDto(product);
    }

    @Transactional
    public OrderDto placeOrder(OrderDto orderDto) {
        Customer customer = customerRepository.findById(orderDto.customerId)
                .orElseThrow(() -> new NotFoundException("customer not found"));
        if (orderDto.lines == null || orderDto.lines.isEmpty()) {
            throw new BadRequestException("order has no lines");
        }

        OrderHeader order = DtoMapper.toDomain(orderDto);

        double subtotal = 0;
        for (OrderLine line : order.lines) {
            Product product = productRepository.findById(line.productId)
                    .orElseThrow(() -> new NotFoundException("product not found"));
            if (line.quantity <= 0) {
                throw new BadRequestException("quantity must be positive");
            }
            if (product.stock < line.quantity) {
                throw new BadRequestException("not enough stock");
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
            Coupon coupon = couponRepository.findByCode(order.couponCode)
                    .orElseThrow(() -> new BadRequestException("unknown coupon"));
            if (coupon.timesUsed >= coupon.maxUses) {
                throw new BadRequestException("coupon has been used up");
            }
            total = total * (100 - coupon.percent) / 100;
            coupon.timesUsed = coupon.timesUsed + 1;
            couponRepository.save(coupon);
        }

        order.id = null;
        order.status = "NEW";
        order.total = total;
        order.surcharge = 0;
        OrderHeader saved = orderRepository.save(order);

        metricsPort.bump("ordersCreated");
        auditPort.add("created order " + saved.id);

        return DtoMapper.toDto(saved);
    }

    public OrderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        return DtoMapper.toDto(order);
    }

    @Transactional
    public OrderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        if (!order.status.equals("NEW")) {
            throw new BadRequestException("only NEW orders can be paid");
        }
        order.status = "PAID";
        order.surcharge = 5.0;
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> new NotFoundException("customer not found"));
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        customerRepository.save(customer);
        OrderHeader saved = orderRepository.save(order);

        metricsPort.bump("ordersPaid");
        auditPort.add("paid order " + order.id);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public OrderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        if (!order.status.equals("PAID")) {
            throw new BadRequestException("only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        OrderHeader saved = orderRepository.save(order);

        metricsPort.bump("ordersShipped");
        auditPort.add("shipped order " + order.id);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        if (order.status.equals("SHIPPED") || order.status.equals("CANCELLED")) {
            throw new BadRequestException("cannot cancel a " + order.status + " order");
        }
        for (OrderLine line : order.lines) {
            Product product = productRepository.findById(line.productId).orElse(null);
            if (product != null) {
                product.stock = product.stock + line.quantity;
                productRepository.save(product);
            }
        }
        order.status = "CANCELLED";
        OrderHeader saved = orderRepository.save(order);

        metricsPort.bump("ordersCancelled");
        auditPort.add("cancelled order " + order.id);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public Map<String, Object> invoiceOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        if (!order.status.equals("PAID") && !order.status.equals("SHIPPED")) {
            throw new BadRequestException("only paid orders can be invoiced");
        }
        long number = invoiceCounterPort.incrementAndGet();
        metricsPort.bump("invoicesIssued");
        auditPort.add("invoiced order " + order.id);

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", number);
        invoice.put("orderId", order.id);
        invoice.put("total", order.total);
        invoice.put("surcharge", order.surcharge);
        invoice.put("amountDue", order.total + order.surcharge);
        return invoice;
    }

    public List<String> getAudit() {
        return auditPort.getAudit();
    }

    public Map<String, Integer> getMetrics() {
        return metricsPort.getMetrics();
    }
}
