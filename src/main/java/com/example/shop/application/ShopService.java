package com.example.shop.application;

import com.example.shop.application.dto.*;
import com.example.shop.application.exception.NotFoundException;
import com.example.shop.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public ShopService(CategoryRepository categoryRepository,
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
        Category category = new Category(null, dto.getName());
        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved.getId(), saved.getName());
    }

    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("category"));
        return new CategoryDto(category.getId(), category.getName());
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = new Customer(null, dto.getName(), 0);
        Customer saved = customerRepository.save(customer);
        return new CustomerDto(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("customer"));
        return new CustomerDto(customer.getId(), customer.getName(), customer.getLoyaltyPoints());
    }

    @Transactional
    public CouponDto createCoupon(CouponDto dto) {
        Coupon coupon = new Coupon(dto.getCode(), dto.getPercent(), dto.getMaxUses(), 0);
        Coupon saved = couponRepository.save(coupon);
        return new CouponDto(saved.getCode(), saved.getPercent(), saved.getMaxUses(), saved.getTimesUsed());
    }

    @Transactional
    public ProductDto createProduct(String name, double price, int stock, Long categoryId) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("category"));
            product.setCategory(category);
        }
        Product saved = productRepository.save(product);
        CategoryDto catDto = null;
        if (saved.getCategory() != null) {
            catDto = new CategoryDto(saved.getCategory().getId(), saved.getCategory().getName());
        }
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock(), catDto);
    }

    public ProductDto getProduct(Long id) {
        Product saved = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("product"));
        CategoryDto catDto = null;
        if (saved.getCategory() != null) {
            catDto = new CategoryDto(saved.getCategory().getId(), saved.getCategory().getName());
        }
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock(), catDto);
    }

    @Transactional
    public OrderDto placeOrder(OrderDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new NotFoundException("customer"));
        if (dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new IllegalArgumentException("order has no lines");
        }

        List<OrderLine> domainLines = new ArrayList<>();
        double subtotal = 0;
        for (OrderLineDto lineDto : dto.getLines()) {
            Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new NotFoundException("product"));
            if (lineDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
            product.decreaseStock(lineDto.getQuantity());

            double linePrice = product.getPrice() * lineDto.getQuantity();
            if (lineDto.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            
            OrderLine line = new OrderLine(null, lineDto.getProductId(), lineDto.getQuantity(), linePrice);
            domainLines.add(line);
            subtotal += linePrice;
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        if (dto.getCouponCode() != null && !dto.getCouponCode().isEmpty()) {
            Coupon coupon = couponRepository.findByCode(dto.getCouponCode())
                    .orElseThrow(() -> new IllegalArgumentException("unknown coupon"));
            if (coupon.isUsedUp()) {
                throw new IllegalArgumentException("coupon has been used up");
            }
            total = total * (100 - coupon.getPercent()) / 100;
            coupon.incrementTimesUsed();
            couponRepository.save(coupon);
        }

        Order order = new Order(null, dto.getCustomerId(), "NEW", total, 0.0, dto.getCouponCode(), domainLines);
        Order saved = orderRepository.save(order);

        metricsPort.bump("ordersCreated");
        auditPort.log("created order " + saved.getId());
        return mapToDto(saved);
    }

    public OrderDto getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order"));
        return mapToDto(order);
    }

    @Transactional
    public OrderDto payOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order"));
        if (!"NEW".equals(order.getStatus())) {
            throw new IllegalArgumentException("only NEW orders can be paid");
        }
        order.setStatus("PAID");
        order.setSurcharge(5.0);
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new NotFoundException("customer"));
        customer.addLoyaltyPoints((int) order.getTotal());
        customerRepository.save(customer);
        Order saved = orderRepository.save(order);

        metricsPort.bump("ordersPaid");
        auditPort.log("paid order " + saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public OrderDto shipOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order"));
        if (!"PAID".equals(order.getStatus())) {
            throw new IllegalArgumentException("only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");
        Order saved = orderRepository.save(order);

        metricsPort.bump("ordersShipped");
        auditPort.log("shipped order " + saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order"));
        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new IllegalArgumentException("cannot cancel a " + order.getStatus() + " order");
        }
        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(product -> {
                product.increaseStock(line.getQuantity());
                productRepository.save(product);
            });
        }
        order.setStatus("CANCELLED");
        Order saved = orderRepository.save(order);

        metricsPort.bump("ordersCancelled");
        auditPort.log("cancelled order " + saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public Map<String, Object> invoiceOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order"));
        if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new IllegalArgumentException("only paid orders can be invoiced");
        }
        long number = invoiceCounterPort.incrementAndGet();
        metricsPort.bump("invoicesIssued");
        auditPort.log("invoiced order " + order.getId());

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", number);
        invoice.put("orderId", order.getId());
        invoice.put("total", order.getTotal());
        invoice.put("surcharge", order.getSurcharge());
        invoice.put("amountDue", order.getTotal() + order.getSurcharge());
        return invoice;
    }

    public List<String> getAuditLogs() {
        return auditPort.getLogs();
    }

    public Map<String, Integer> getMetrics() {
        return metricsPort.getMetrics();
    }

    private OrderDto mapToDto(Order order) {
        List<OrderLineDto> lines = order.getLines().stream()
                .map(line -> new OrderLineDto(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderDto(order.getId(), order.getCustomerId(), order.getStatus(), order.getTotal(), order.getSurcharge(), order.getCouponCode(), lines);
    }
}
