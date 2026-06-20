package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final SystemStateRepository systemStateRepository;

    public ShopService(CategoryRepository categoryRepository,
                       CustomerRepository customerRepository,
                       CouponRepository couponRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       SystemStateRepository systemStateRepository) {
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.systemStateRepository = systemStateRepository;
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = new Category();
        category.name = dto.name;
        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> notFound("category"));
        return mapToDto(category);
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = new Customer();
        customer.name = dto.name;
        customer.loyaltyPoints = 0;
        Customer saved = customerRepository.save(customer);
        return mapToDto(saved);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> notFound("customer"));
        return mapToDto(customer);
    }

    @Transactional
    public CouponDto createCoupon(CouponDto dto) {
        Coupon coupon = new Coupon();
        coupon.code = dto.code;
        coupon.percent = dto.percent;
        coupon.maxUses = dto.maxUses;
        coupon.timesUsed = 0;
        Coupon saved = couponRepository.save(coupon);
        return mapToDto(saved);
    }

    @Transactional
    public ProductDto createProduct(ProductCreateDto dto) {
        Product product = new Product();
        product.name = dto.name;
        product.price = dto.price;
        product.stock = dto.stock;
        if (dto.categoryId != null) {
            Category category = categoryRepository.findById(dto.categoryId)
                    .orElseThrow(() -> notFound("category"));
            product.category = category;
        }
        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> notFound("product"));
        return mapToDto(product);
    }

    @Transactional
    public OrderDto placeOrder(OrderDto dto) {
        Customer customer = customerRepository.findById(dto.customerId)
                .orElseThrow(() -> notFound("customer"));
        if (dto.lines == null || dto.lines.isEmpty()) {
            throw badRequest("order has no lines");
        }

        OrderHeader order = new OrderHeader();
        order.customerId = dto.customerId;
        order.couponCode = dto.couponCode;

        double subtotal = 0;
        for (OrderLineDto lineDto : dto.lines) {
            Product product = productRepository.findById(lineDto.productId)
                    .orElseThrow(() -> notFound("product"));
            if (lineDto.quantity <= 0) {
                throw badRequest("quantity must be positive");
            }
            if (product.stock < lineDto.quantity) {
                throw badRequest("not enough stock");
            }

            double linePrice = product.price * lineDto.quantity;
            if (lineDto.quantity >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            
            OrderLine line = new OrderLine();
            line.productId = lineDto.productId;
            line.quantity = lineDto.quantity;
            line.linePrice = linePrice;
            order.lines.add(line);

            subtotal = subtotal + linePrice;
            product.stock = product.stock - lineDto.quantity;
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        if (order.couponCode != null && !order.couponCode.isEmpty()) {
            Coupon coupon = couponRepository.findByCode(order.couponCode)
                    .orElseThrow(() -> badRequest("unknown coupon"));
            if (coupon.timesUsed >= coupon.maxUses) {
                throw badRequest("coupon has been used up");
            }
            total = total * (100 - coupon.percent) / 100;
            coupon.timesUsed = coupon.timesUsed + 1;
            couponRepository.save(coupon);
        }

        order.status = "NEW";
        order.total = total;
        order.surcharge = 0;
        OrderHeader saved = orderRepository.save(order);

        systemStateRepository.bumpMetric("ordersCreated");
        systemStateRepository.addAudit("created order " + saved.id);

        return mapToDto(saved);
    }

    public OrderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        return mapToDto(order);
    }

    @Transactional
    public OrderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        if (!order.status.equals("NEW")) {
            throw badRequest("only NEW orders can be paid");
        }
        order.status = "PAID";
        order.surcharge = 5.0;
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> notFound("customer"));
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        customerRepository.save(customer);
        OrderHeader saved = orderRepository.save(order);

        systemStateRepository.bumpMetric("ordersPaid");
        systemStateRepository.addAudit("paid order " + saved.id);

        return mapToDto(saved);
    }

    @Transactional
    public OrderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        if (!order.status.equals("PAID")) {
            throw badRequest("only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        OrderHeader saved = orderRepository.save(order);

        systemStateRepository.bumpMetric("ordersShipped");
        systemStateRepository.addAudit("shipped order " + saved.id);

        return mapToDto(saved);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
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
        OrderHeader saved = orderRepository.save(order);

        systemStateRepository.bumpMetric("ordersCancelled");
        systemStateRepository.addAudit("cancelled order " + saved.id);

        return mapToDto(saved);
    }

    @Transactional
    public Map<String, Object> invoiceOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        if (!order.status.equals("PAID") && !order.status.equals("SHIPPED")) {
            throw badRequest("only paid orders can be invoiced");
        }
        long number = systemStateRepository.incrementInvoiceCounter();
        systemStateRepository.bumpMetric("invoicesIssued");
        systemStateRepository.addAudit("invoiced order " + order.id);

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", number);
        invoice.put("orderId", order.id);
        invoice.put("total", order.total);
        invoice.put("surcharge", order.surcharge);
        invoice.put("amountDue", order.total + order.surcharge);
        return invoice;
    }

    public List<String> getAudit() {
        return systemStateRepository.getAudit();
    }

    public Map<String, Integer> getMetrics() {
        return systemStateRepository.getMetrics();
    }

    private CategoryDto mapToDto(Category category) {
        if (category == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.id = category.id;
        dto.name = category.name;
        return dto;
    }

    private CustomerDto mapToDto(Customer customer) {
        if (customer == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.id = customer.id;
        dto.name = customer.name;
        dto.loyaltyPoints = customer.loyaltyPoints;
        return dto;
    }

    private CouponDto mapToDto(Coupon coupon) {
        if (coupon == null) return null;
        CouponDto dto = new CouponDto();
        dto.code = coupon.code;
        dto.percent = coupon.percent;
        dto.maxUses = coupon.maxUses;
        dto.timesUsed = coupon.timesUsed;
        return dto;
    }

    private ProductDto mapToDto(Product product) {
        if (product == null) return null;
        ProductDto dto = new ProductDto();
        dto.id = product.id;
        dto.name = product.name;
        dto.price = product.price;
        dto.stock = product.stock;
        dto.category = mapToDto(product.category);
        return dto;
    }

    private OrderDto mapToDto(OrderHeader order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        dto.id = order.id;
        dto.customerId = order.customerId;
        dto.status = order.status;
        dto.total = order.total;
        dto.surcharge = order.surcharge;
        dto.couponCode = order.couponCode;
        if (order.lines != null) {
            dto.lines = order.lines.stream().map(line -> {
                OrderLineDto lineDto = new OrderLineDto();
                lineDto.id = line.id;
                lineDto.productId = line.productId;
                lineDto.quantity = line.quantity;
                lineDto.linePrice = line.linePrice;
                return lineDto;
            }).collect(Collectors.toList());
        }
        return dto;
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
