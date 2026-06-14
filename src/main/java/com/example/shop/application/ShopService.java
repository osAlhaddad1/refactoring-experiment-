package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ShopService {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditPort auditPort;
    private final MetricsPort metricsPort;
    private final InvoicePort invoicePort;

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

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = toDomain(categoryDto);
        category.id = null;
        return toDto(categoryRepository.save(category));
    }

    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));
        return toDto(category);
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        Customer customer = toDomain(customerDto);
        customer.id = null;
        customer.loyaltyPoints = 0;
        return toDto(customerRepository.save(customer));
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        return toDto(customer);
    }

    @Transactional
    public CouponDto createCoupon(CouponDto couponDto) {
        Coupon coupon = toDomain(couponDto);
        coupon.timesUsed = 0;
        return toDto(couponRepository.save(coupon));
    }

    @Transactional
    public ProductDto createProduct(String name, double price, int stock, Long categoryId) {
        Product product = new Product();
        product.name = name;
        product.price = price;
        product.stock = stock;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));
            product.category = category;
        }
        return toDto(productRepository.save(product));
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return toDto(product);
    }

    @Transactional
    public OrderHeaderDto placeOrder(OrderHeaderDto orderDto) {
        OrderHeader order = toDomain(orderDto);
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
            Coupon coupon = couponRepository.findByCode(order.couponCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown coupon"));
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
        metricsPort.bump("ordersCreated");
        auditPort.log("created order " + savedOrder.id);
        return toDto(savedOrder);
    }

    public OrderHeaderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return toDto(order);
    }

    @Transactional
    public OrderHeaderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        if (!"NEW".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only NEW orders can be paid");
        }
        order.status = "PAID";
        order.surcharge = 5.0;
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersPaid");
        auditPort.log("paid order " + savedOrder.id);
        return toDto(savedOrder);
    }

    @Transactional
    public OrderHeaderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        if (!"PAID".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersShipped");
        auditPort.log("shipped order " + savedOrder.id);
        return toDto(savedOrder);
    }

    @Transactional
    public OrderHeaderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        if ("SHIPPED".equals(order.status) || "CANCELLED".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot cancel a " + order.status + " order");
        }
        for (OrderLine line : order.lines) {
            Optional<Product> productOpt = productRepository.findById(line.productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.stock = product.stock + line.quantity;
                productRepository.save(product);
            }
        }
        order.status = "CANCELLED";
        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersCancelled");
        auditPort.log("cancelled order " + savedOrder.id);
        return toDto(savedOrder);
    }

    @Transactional
    public Map<String, Object> invoiceOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        if (!"PAID".equals(order.status) && !"SHIPPED".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only paid orders can be invoiced");
        }
        long number = invoicePort.nextInvoiceNumber();
        metricsPort.bump("invoicesIssued");
        auditPort.log("invoiced order " + order.id);

        Map<String, Object> invoice = new java.util.LinkedHashMap<>();
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

    private Category toDomain(CategoryDto dto) {
        if (dto == null) return null;
        Category category = new Category();
        category.id = dto.id;
        category.name = dto.name;
        return category;
    }

    private CategoryDto toDto(Category category) {
        if (category == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.id = category.id;
        dto.name = category.name;
        return dto;
    }

    private Customer toDomain(CustomerDto dto) {
        if (dto == null) return null;
        Customer customer = new Customer();
        customer.id = dto.id;
        customer.name = dto.name;
        customer.loyaltyPoints = dto.loyaltyPoints;
        return customer;
    }

    private CustomerDto toDto(Customer customer) {
        if (customer == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.id = customer.id;
        dto.name = customer.name;
        dto.loyaltyPoints = customer.loyaltyPoints;
        return dto;
    }

    private Coupon toDomain(CouponDto dto) {
        if (dto == null) return null;
        Coupon coupon = new Coupon();
        coupon.code = dto.code;
        coupon.percent = dto.percent;
        coupon.maxUses = dto.maxUses;
        coupon.timesUsed = dto.timesUsed;
        return coupon;
    }

    private CouponDto toDto(Coupon coupon) {
        if (coupon == null) return null;
        CouponDto dto = new CouponDto();
        dto.code = coupon.code;
        dto.percent = coupon.percent;
        dto.maxUses = coupon.maxUses;
        dto.timesUsed = coupon.timesUsed;
        return dto;
    }

    private ProductDto toDto(Product product) {
        if (product == null) return null;
        ProductDto dto = new ProductDto();
        dto.id = product.id;
        dto.name = product.name;
        dto.price = product.price;
        dto.stock = product.stock;
        dto.category = toDto(product.category);
        return dto;
    }

    private OrderHeader toDomain(OrderHeaderDto dto) {
        if (dto == null) return null;
        OrderHeader order = new OrderHeader();
        order.id = dto.id;
        order.customerId = dto.customerId;
        order.status = dto.status;
        order.total = dto.total;
        order.surcharge = dto.surcharge;
        order.couponCode = dto.couponCode;
        if (dto.lines != null) {
            for (OrderLineDto lineDto : dto.lines) {
                OrderLine line = new OrderLine();
                line.id = lineDto.id;
                line.productId = lineDto.productId;
                line.quantity = lineDto.quantity;
                line.linePrice = lineDto.linePrice;
                order.lines.add(line);
            }
        }
        return order;
    }

    private OrderHeaderDto toDto(OrderHeader order) {
        if (order == null) return null;
        OrderHeaderDto dto = new OrderHeaderDto();
        dto.id = order.id;
        dto.customerId = order.customerId;
        dto.status = order.status;
        dto.total = order.total;
        dto.surcharge = order.surcharge;
        dto.couponCode = order.couponCode;
        if (order.lines != null) {
            for (OrderLine line : order.lines) {
                OrderLineDto lineDto = new OrderLineDto();
                lineDto.id = line.id;
                lineDto.productId = line.productId;
                lineDto.quantity = line.quantity;
                lineDto.linePrice = line.linePrice;
                dto.lines.add(lineDto);
            }
        }
        return dto;
    }
}
