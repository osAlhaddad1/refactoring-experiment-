package com.example.shop.application;

import com.example.shop.domain.*;
import com.example.shop.domain.exception.BadRequestException;
import com.example.shop.domain.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ShopApplicationService {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditPort auditPort;
    private final MetricsPort metricsPort;
    private final InvoicePort invoicePort;
    private final OrderDomainService orderDomainService = new OrderDomainService();

    public ShopApplicationService(
            CategoryRepository categoryRepository,
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

    // ----- categories -----

    @Transactional
    public Map<String, Object> createCategory(String name) {
        Category category = new Category(null, name);
        Category saved = categoryRepository.save(category);
        return mapCategory(saved);
    }

    public Map<String, Object> getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));
        return mapCategory(category);
    }

    private Map<String, Object> mapCategory(Category category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        return map;
    }

    // ----- customers -----

    @Transactional
    public Map<String, Object> createCustomer(String name) {
        Customer customer = new Customer(null, name, 0);
        Customer saved = customerRepository.save(customer);
        return mapCustomer(saved);
    }

    public Map<String, Object> getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        return mapCustomer(customer);
    }

    private Map<String, Object> mapCustomer(Customer customer) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", customer.getId());
        map.put("name", customer.getName());
        map.put("loyaltyPoints", customer.getLoyaltyPoints());
        return map;
    }

    // ----- coupons -----

    @Transactional
    public Map<String, Object> createCoupon(String code, int percent, int maxUses) {
        Coupon coupon = new Coupon(code, percent, maxUses, 0);
        Coupon saved = couponRepository.save(coupon);
        return mapCoupon(saved);
    }

    private Map<String, Object> mapCoupon(Coupon coupon) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", coupon.getCode());
        map.put("percent", coupon.getPercent());
        map.put("maxUses", coupon.getMaxUses());
        map.put("timesUsed", coupon.getTimesUsed());
        return map;
    }

    // ----- products -----

    @Transactional
    public Map<String, Object> createProduct(String name, double price, int stock, Long categoryId) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));
        }
        Product product = new Product(null, name, price, stock, category);
        Product saved = productRepository.save(product);
        return mapProduct(saved);
    }

    public Map<String, Object> getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return mapProduct(product);
    }

    private Map<String, Object> mapProduct(Product product) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("price", product.getPrice());
        map.put("stock", product.getStock());
        if (product.getCategory() != null) {
            map.put("category", mapCategory(product.getCategory()));
        } else {
            map.put("category", null);
        }
        return map;
    }

    // ----- orders -----

    @Transactional
    public Map<String, Object> placeOrder(Long customerId, String couponCode, List<Map<String, Object>> linesData) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));

        if (linesData == null || linesData.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order has no lines");
        }

        List<OrderLine> lines = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        for (Map<String, Object> lineMap : linesData) {
            Long productId = ((Number) lineMap.get("productId")).longValue();
            int quantity = ((Number) lineMap.get("quantity")).intValue();
            OrderLine line = new OrderLine(null, productId, quantity, 0.0);
            lines.add(line);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
            products.add(product);
        }

        Coupon coupon = null;
        if (couponCode != null && !couponCode.isEmpty()) {
            coupon = couponRepository.findByCode(couponCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown coupon"));
        }

        OrderHeader order = new OrderHeader(null, customerId, null, 0.0, 0.0, couponCode, lines);

        try {
            orderDomainService.placeOrder(order, customer, products, coupon);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        for (Product product : products) {
            productRepository.save(product);
        }

        if (coupon != null) {
            couponRepository.save(coupon);
        }

        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersCreated");
        auditPort.add("created order " + savedOrder.getId());
        return mapOrder(savedOrder);
    }

    public Map<String, Object> getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return mapOrder(order);
    }

    @Transactional
    public Map<String, Object> payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));

        try {
            orderDomainService.payOrder(order, customer);
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        customerRepository.save(customer);
        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersPaid");
        auditPort.add("paid order " + savedOrder.getId());
        return mapOrder(savedOrder);
    }

    @Transactional
    public Map<String, Object> shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        try {
            orderDomainService.shipOrder(order);
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersShipped");
        auditPort.add("shipped order " + savedOrder.getId());
        return mapOrder(savedOrder);
    }

    @Transactional
    public Map<String, Object> cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        List<Product> products = new ArrayList<>();
        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(products::add);
        }

        try {
            orderDomainService.cancelOrder(order, products);
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        for (Product product : products) {
            productRepository.save(product);
        }

        OrderHeader savedOrder = orderRepository.save(order);
        metricsPort.bump("ordersCancelled");
        auditPort.add("cancelled order " + savedOrder.getId());
        return mapOrder(savedOrder);
    }

    @Transactional
    public Map<String, Object> invoiceOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only paid orders can be invoiced");
        }

        long number = invoicePort.nextInvoiceNumber();
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

    private Map<String, Object> mapOrder(OrderHeader order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId());
        map.put("customerId", order.getCustomerId());
        map.put("status", order.getStatus());
        map.put("total", order.getTotal());
        map.put("surcharge", order.getSurcharge());
        map.put("couponCode", order.getCouponCode());
        List<Map<String, Object>> linesList = new ArrayList<>();
        if (order.getLines() != null) {
            for (OrderLine line : order.getLines()) {
                Map<String, Object> lineMap = new LinkedHashMap<>();
                lineMap.put("id", line.getId());
                lineMap.put("productId", line.getProductId());
                lineMap.put("quantity", line.getQuantity());
                lineMap.put("linePrice", line.getLinePrice());
                linesList.add(lineMap);
            }
        }
        map.put("lines", linesList);
        return map;
    }

    // ----- audit + metrics -----

    public List<String> getAudit() {
        return auditPort.getAudit();
    }

    public Map<String, Integer> getMetrics() {
        return metricsPort.getMetrics();
    }
}