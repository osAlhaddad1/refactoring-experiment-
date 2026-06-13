package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShopService {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditLogPort auditLogPort;
    private final MetricsPort metricsPort;
    private final InvoiceCounterPort invoiceCounterPort;

    public ShopService(
            CategoryRepository categoryRepository,
            CustomerRepository customerRepository,
            CouponRepository couponRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            AuditLogPort auditLogPort,
            MetricsPort metricsPort,
            InvoiceCounterPort invoiceCounterPort) {
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditLogPort = auditLogPort;
        this.metricsPort = metricsPort;
        this.invoiceCounterPort = invoiceCounterPort;
    }

    public CategoryDto createCategory(CategoryDto dto) {
        Category category = new Category(null, dto.name);
        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved.getId(), saved.getName());
    }

    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));
        return new CategoryDto(category.getId(), category.getName());
    }

    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = new Customer(null, dto.name, 0);
        Customer saved = customerRepository.save(customer);
        return new CustomerDto(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        return new CustomerDto(customer.getId(), customer.getName(), customer.getLoyaltyPoints());
    }

    public CouponDto createCoupon(CouponDto dto) {
        Coupon coupon = new Coupon(dto.code, dto.percent, dto.maxUses, 0);
        Coupon saved = couponRepository.save(coupon);
        return new CouponDto(saved.getCode(), saved.getPercent(), saved.getMaxUses(), saved.getTimesUsed());
    }

    public ProductDto createProduct(String name, double price, int stock, Long categoryId) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));
        }
        Product product = new Product(null, name, price, stock, category);
        Product saved = productRepository.save(product);
        CategoryDto catDto = saved.getCategory() != null ? new CategoryDto(saved.getCategory().getId(), saved.getCategory().getName()) : null;
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock(), catDto);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        CategoryDto catDto = product.getCategory() != null ? new CategoryDto(product.getCategory().getId(), product.getCategory().getName()) : null;
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock(), catDto);
    }

    public OrderHeaderDto placeOrder(OrderHeaderDto orderDto) {
        Customer customer = customerRepository.findById(orderDto.customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));

        if (orderDto.lines == null || orderDto.lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order has no lines");
        }

        List<Product> products = new ArrayList<>();
        for (OrderLineDto lineDto : orderDto.lines) {
            Product product = productRepository.findById(lineDto.productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
            products.add(product);
        }

        Coupon coupon = null;
        if (orderDto.couponCode != null && !orderDto.couponCode.isEmpty()) {
            coupon = couponRepository.findByCode(orderDto.couponCode).orElse(null);
        }

        List<OrderLine> lines = orderDto.lines.stream()
                .map(l -> new OrderLine(null, l.productId, l.quantity, 0))
                .collect(Collectors.toList());

        OrderHeader order = new OrderHeader(null, orderDto.customerId, "NEW", 0, 0, orderDto.couponCode, lines);

        try {
            OrderProcessor.processOrderPlacement(order, customer, products, coupon);
        } catch (DomainException e) {
            if (e.isNotFound()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        for (Product product : products) {
            productRepository.save(product);
        }

        if (coupon != null) {
            couponRepository.save(coupon);
        }

        OrderHeader savedOrder = orderRepository.save(order);

        metricsPort.bump("ordersCreated");
        auditLogPort.log("created order " + savedOrder.getId());

        return mapToOrderDto(savedOrder);
    }

    public OrderHeaderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return mapToOrderDto(order);
    }

    public OrderHeaderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));

        try {
            OrderProcessor.payOrder(order, customer);
        } catch (DomainException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);

        metricsPort.bump("ordersPaid");
        auditLogPort.log("paid order " + savedOrder.getId());

        return mapToOrderDto(savedOrder);
    }

    public OrderHeaderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        try {
            OrderProcessor.shipOrder(order);
        } catch (DomainException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        OrderHeader savedOrder = orderRepository.save(order);

        metricsPort.bump("ordersShipped");
        auditLogPort.log("shipped order " + savedOrder.getId());

        return mapToOrderDto(savedOrder);
    }

    public OrderHeaderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        List<Product> products = new ArrayList<>();
        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(products::add);
        }

        try {
            OrderProcessor.cancelOrder(order, products);
        } catch (DomainException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        for (Product product : products) {
            productRepository.save(product);
        }

        OrderHeader savedOrder = orderRepository.save(order);

        metricsPort.bump("ordersCancelled");
        auditLogPort.log("cancelled order " + savedOrder.getId());

        return mapToOrderDto(savedOrder);
    }

    public InvoiceDto invoiceOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only paid orders can be invoiced");
        }

        long number = invoiceCounterPort.incrementAndGet();
        metricsPort.bump("invoicesIssued");
        auditLogPort.log("invoiced order " + order.getId());

        return new InvoiceDto(number, order.getId(), order.getTotal(), order.getSurcharge(), order.getTotal() + order.getSurcharge());
    }

    public List<String> getAuditLogs() {
        return auditLogPort.getLogs();
    }

    public Map<String, Integer> getMetrics() {
        return metricsPort.getMetrics();
    }

    private OrderHeaderDto mapToOrderDto(OrderHeader order) {
        List<OrderLineDto> lineDtos = order.getLines().stream()
                .map(l -> new OrderLineDto(l.getId(), l.getProductId(), l.getQuantity(), l.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderHeaderDto(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotal(),
                order.getSurcharge(),
                order.getCouponCode(),
                lineDtos
        );
    }
}
