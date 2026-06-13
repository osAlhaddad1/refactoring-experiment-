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
    private final SystemStatePort systemStatePort;

    public ShopService(CategoryRepository categoryRepository,
                       CustomerRepository customerRepository,
                       CouponRepository couponRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       SystemStatePort systemStatePort) {
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.systemStatePort = systemStatePort;
    }

    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = mapCategory(categoryDto);
        category.id = null;
        Category saved = categoryRepository.save(category);
        return mapCategory(saved);
    }

    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            throw new NotFoundException("category not found");
        }
        return mapCategory(category);
    }

    public CustomerDto createCustomer(CustomerDto customerDto) {
        Customer customer = mapCustomer(customerDto);
        customer.id = null;
        customer.loyaltyPoints = 0;
        Customer saved = customerRepository.save(customer);
        return mapCustomer(saved);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id);
        if (customer == null) {
            throw new NotFoundException("customer not found");
        }
        return mapCustomer(customer);
    }

    public CouponDto createCoupon(CouponDto couponDto) {
        Coupon coupon = mapCoupon(couponDto);
        coupon.timesUsed = 0;
        Coupon saved = couponRepository.save(coupon);
        return mapCoupon(saved);
    }

    public ProductDto createProduct(String name, double price, int stock, Long categoryId) {
        Product product = new Product();
        product.name = name;
        product.price = price;
        product.stock = stock;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                throw new NotFoundException("category not found");
            }
            product.category = category;
        }
        Product saved = productRepository.save(product);
        return mapProduct(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new NotFoundException("product not found");
        }
        return mapProduct(product);
    }

    public OrderDto placeOrder(OrderDto orderDto) {
        OrderHeader order = new OrderHeader();
        order.customerId = orderDto.customerId;
        order.couponCode = orderDto.couponCode;
        order.lines = new ArrayList<>();
        if (orderDto.lines != null) {
            for (OrderLineDto lineDto : orderDto.lines) {
                OrderLine line = new OrderLine();
                line.productId = lineDto.productId;
                line.quantity = lineDto.quantity;
                order.lines.add(line);
            }
        }

        OrderHeader saved = placeOrderEntity(order);
        return mapOrder(saved);
    }

    public OrderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id);
        if (order == null) {
            throw new NotFoundException("order not found");
        }
        return mapOrder(order);
    }

    public OrderDto payOrder(Long id) {
        OrderHeader order = payOrderEntity(id);
        return mapOrder(order);
    }

    public OrderDto shipOrder(Long id) {
        OrderHeader order = shipOrderEntity(id);
        return mapOrder(order);
    }

    public OrderDto cancelOrder(Long id) {
        OrderHeader order = cancelOrderEntity(id);
        return mapOrder(order);
    }

    public Map<String, Object> invoiceOrder(Long id) {
        OrderHeader order = orderRepository.findById(id);
        if (order == null) {
            throw new NotFoundException("order not found");
        }
        if (!"PAID".equals(order.status) && !"SHIPPED".equals(order.status)) {
            throw new BadRequestException("only paid orders can be invoiced");
        }
        long number = systemStatePort.nextInvoiceNumber();
        systemStatePort.incrementMetric("invoicesIssued");
        systemStatePort.addAuditLog("invoiced order " + order.id);

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", number);
        invoice.put("orderId", order.id);
        invoice.put("total", order.total);
        invoice.put("surcharge", order.surcharge);
        invoice.put("amountDue", order.total + order.surcharge);
        return invoice;
    }

    public List<String> getAuditLogs() {
        return systemStatePort.getAuditLogs();
    }

    public Map<String, Integer> getMetrics() {
        return systemStatePort.getMetrics();
    }

    private OrderHeader placeOrderEntity(OrderHeader order) {
        Customer customer = customerRepository.findById(order.customerId);
        if (customer == null) {
            throw new NotFoundException("customer not found");
        }
        if (order.lines == null || order.lines.isEmpty()) {
            throw new BadRequestException("order has no lines");
        }

        double subtotal = 0;
        for (OrderLine line : order.lines) {
            Product product = productRepository.findById(line.productId);
            if (product == null) {
                throw new NotFoundException("product not found");
            }
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
            Coupon coupon = couponRepository.findByCode(order.couponCode);
            if (coupon == null) {
                throw new BadRequestException("unknown coupon");
            }
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
        OrderHeader savedOrder = orderRepository.save(order);

        systemStatePort.incrementMetric("ordersCreated");
        systemStatePort.addAuditLog("created order " + savedOrder.id);

        return savedOrder;
    }

    private OrderHeader payOrderEntity(Long id) {
        OrderHeader order = orderRepository.findById(id);
        if (order == null) {
            throw new NotFoundException("order not found");
        }
        if (!"NEW".equals(order.status)) {
            throw new BadRequestException("only NEW orders can be paid");
        }
        order.status = "PAID";
        order.surcharge = 5.0;
        Customer customer = customerRepository.findById(order.customerId);
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);

        systemStatePort.incrementMetric("ordersPaid");
        systemStatePort.addAuditLog("paid order " + order.id);
        return savedOrder;
    }

    private OrderHeader shipOrderEntity(Long id) {
        OrderHeader order = orderRepository.findById(id);
        if (order == null) {
            throw new NotFoundException("order not found");
        }
        if (!"PAID".equals(order.status)) {
            throw new BadRequestException("only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        OrderHeader savedOrder = orderRepository.save(order);

        systemStatePort.incrementMetric("ordersShipped");
        systemStatePort.addAuditLog("shipped order " + order.id);
        return savedOrder;
    }

    private OrderHeader cancelOrderEntity(Long id) {
        OrderHeader order = orderRepository.findById(id);
        if (order == null) {
            throw new NotFoundException("order not found");
        }
        if ("SHIPPED".equals(order.status) || "CANCELLED".equals(order.status)) {
            throw new BadRequestException("cannot cancel a " + order.status + " order");
        }

        for (OrderLine line : order.lines) {
            Product product = productRepository.findById(line.productId);
            if (product != null) {
                product.stock = product.stock + line.quantity;
                productRepository.save(product);
            } 
        }
        order.status = "CANCELLED";
        OrderHeader savedOrder = orderRepository.save(order);

        systemStatePort.incrementMetric("ordersCancelled");
        systemStatePort.addAuditLog("cancelled order " + order.id);
        return savedOrder;
    }

    private Category mapCategory(CategoryDto dto) {
        if (dto == null) return null;
        Category category = new Category();
        category.id = dto.id;
        category.name = dto.name;
        return category;
    }

    private CategoryDto mapCategory(Category category) {
        if (category == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.id = category.id;
        dto.name = category.name;
        return dto;
    }

    private Customer mapCustomer(CustomerDto dto) {
        if (dto == null) return null;
        Customer customer = new Customer();
        customer.id = dto.id;
        customer.name = dto.name;
        customer.loyaltyPoints = dto.loyaltyPoints;
        return customer;
    }

    private CustomerDto mapCustomer(Customer customer) {
        if (customer == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.id = customer.id;
        dto.name = customer.name;
        dto.loyaltyPoints = customer.loyaltyPoints;
        return dto;
    }

    private Coupon mapCoupon(CouponDto dto) {
        if (dto == null) return null;
        Coupon coupon = new Coupon();
        coupon.code = dto.code;
        coupon.percent = dto.percent;
        coupon.maxUses = dto.maxUses;
        coupon.timesUsed = dto.timesUsed;
        return coupon;
    }

    private CouponDto mapCoupon(Coupon coupon) {
        if (coupon == null) return null;
        CouponDto dto = new CouponDto();
        dto.code = coupon.code;
        dto.percent = coupon.percent;
        dto.maxUses = coupon.maxUses;
        dto.timesUsed = coupon.timesUsed;
        return dto;
    }

    private ProductDto mapProduct(Product product) {
        if (product == null) return null;
        ProductDto dto = new ProductDto();
        dto.id = product.id;
        dto.name = product.name;
        dto.price = product.price;
        dto.stock = product.stock;
        dto.category = mapCategory(product.category);
        return dto;
    }

    private OrderDto mapOrder(OrderHeader order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        dto.id = order.id;
        dto.customerId = order.customerId;
        dto.status = order.status;
        dto.total = order.total;
        dto.surcharge = order.surcharge;
        dto.couponCode = order.couponCode;
        dto.lines = new ArrayList<>();
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