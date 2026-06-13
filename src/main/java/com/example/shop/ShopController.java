package com.example.shop;

import jakarta.persistence.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * XCOMPLEX BASELINE "god file".
 *
 * The biggest mess: one class in the root package that mixes HTTP handling, a
 * multi-step pricing engine (per-line bulk discount, order-level discount, and a
 * usage-limited coupon), an order state machine (pay / ship / cancel / restock)
 * with a payment surcharge, customer loyalty, a JPA model with a one-to-many
 * (Order -> OrderLine) and a many-to-one (Product -> Category), and THREE global
 * statics: an audit log, a metrics map, and an invoice counter.
 *
 * It works (the HTTP tests pass) but it has no architecture, so the ArchUnit
 * gate fails on it (a *Controller is not allowed in the root package).
 */
@RestController
public class ShopController {

    // Three pieces of global static state (each an anti-pattern a refactoring
    // would have to hide behind a port).
    static final List<String> AUDIT = new ArrayList<>();
    static final Map<String, Integer> METRICS = new HashMap<>();
    static long INVOICE_COUNTER = 0;

    @PersistenceContext
    private EntityManager em;

    // ----- categories -----------------------------------------------------

    @PostMapping("/categories")
    @Transactional
    public Category createCategory(@RequestBody Category category) {
        category.id = null;
        em.persist(category);
        return category;
    }

    @GetMapping("/categories/{id}")
    public Category getCategory(@PathVariable Long id) {
        Category category = em.find(Category.class, id);
        if (category == null) {
            throw notFound("category");
        }
        return category;
    }

    // ----- customers ------------------------------------------------------

    @PostMapping("/customers")
    @Transactional
    public Customer createCustomer(@RequestBody Customer customer) {
        customer.id = null;
        customer.loyaltyPoints = 0;
        em.persist(customer);
        return customer;
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        Customer customer = em.find(Customer.class, id);
        if (customer == null) {
            throw notFound("customer");
        }
        return customer;
    }

    // ----- coupons --------------------------------------------------------

    @PostMapping("/coupons")
    @Transactional
    public Coupon createCoupon(@RequestBody Coupon coupon) {
        coupon.timesUsed = 0;
        em.persist(coupon);
        return coupon;
    }

    // ----- products -------------------------------------------------------

    @PostMapping("/products")
    @Transactional
    public Product createProduct(@RequestBody Map<String, Object> body) {
        Product product = new Product();
        product.name = (String) body.get("name");
        product.price = ((Number) body.get("price")).doubleValue();
        product.stock = ((Number) body.get("stock")).intValue();
        Object categoryId = body.get("categoryId");
        if (categoryId != null) {
            Category category = em.find(Category.class, ((Number) categoryId).longValue());
            if (category == null) {
                throw notFound("category");
            }
            product.category = category;
        }
        em.persist(product);
        return product;
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        Product product = em.find(Product.class, id);
        if (product == null) {
            throw notFound("product");
        }
        return product;
    }

    // ----- orders ---------------------------------------------------------

    @PostMapping("/orders")
    @Transactional
    public OrderHeader placeOrder(@RequestBody OrderHeader order) {
        Customer customer = em.find(Customer.class, order.customerId);
        if (customer == null) {
            throw notFound("customer");
        }
        if (order.lines == null || order.lines.isEmpty()) {
            throw badRequest("order has no lines");
        }

        // Multi-step pricing engine.
        // step 1: each line price, with a per-line bulk discount (10+ items -> 10% off the line)
        double subtotal = 0;
        for (OrderLine line : order.lines) {
            Product product = em.find(Product.class, line.productId);
            if (product == null) {
                throw notFound("product");
            }
            if (line.quantity <= 0) {
                throw badRequest("quantity must be positive");
            }
            if (product.stock < line.quantity) {
                throw badRequest("not enough stock");
            }

            double linePrice = product.price * line.quantity;
            if (line.quantity >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            line.linePrice = linePrice;
            subtotal = subtotal + linePrice;
            product.stock = product.stock - line.quantity;
        }
        // step 2: an order-level discount for big orders (subtotal 500+ -> 5% off)
        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }
        // step 3: a usage-limited coupon, if one was given
        if (order.couponCode != null && !order.couponCode.isEmpty()) {
            Coupon coupon = findCoupon(order.couponCode);
            if (coupon == null) {
                throw badRequest("unknown coupon");
            }
            if (coupon.timesUsed >= coupon.maxUses) {
                throw badRequest("coupon has been used up");
            }
            total = total * (100 - coupon.percent) / 100;
            coupon.timesUsed = coupon.timesUsed + 1;
        }

        order.id = null;
        order.status = "NEW";
        order.total = total;
        order.surcharge = 0;
        em.persist(order);
        bump("ordersCreated");
        AUDIT.add("created order " + order.id);
        return order;
    }

    @GetMapping("/orders/{id}")
    public OrderHeader getOrder(@PathVariable Long id) {
        return requireOrder(id);
    }

    @PostMapping("/orders/{id}/pay")
    @Transactional
    public OrderHeader payOrder(@PathVariable Long id) {
        OrderHeader order = requireOrder(id);
        if (!order.status.equals("NEW")) {
            throw badRequest("only NEW orders can be paid");
        }
        order.status = "PAID";
        order.surcharge = 5.0;   // flat payment surcharge
        Customer customer = em.find(Customer.class, order.customerId);
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        bump("ordersPaid");
        AUDIT.add("paid order " + order.id);
        return order;
    }

    @PostMapping("/orders/{id}/ship")
    @Transactional
    public OrderHeader shipOrder(@PathVariable Long id) {
        OrderHeader order = requireOrder(id);
        if (!order.status.equals("PAID")) {
            throw badRequest("only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        bump("ordersShipped");
        AUDIT.add("shipped order " + order.id);
        return order;
    }

    @PostMapping("/orders/{id}/cancel")
    @Transactional
    public OrderHeader cancelOrder(@PathVariable Long id) {
        OrderHeader order = requireOrder(id);
        if (order.status.equals("SHIPPED") || order.status.equals("CANCELLED")) {
            throw badRequest("cannot cancel a " + order.status + " order");
        }
        // restock the items
        for (OrderLine line : order.lines) {
            Product product = em.find(Product.class, line.productId);
            if (product != null) {
                product.stock = product.stock + line.quantity;
            }
        }
        order.status = "CANCELLED";
        bump("ordersCancelled");
        AUDIT.add("cancelled order " + order.id);
        return order;
    }

    @PostMapping("/orders/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        OrderHeader order = requireOrder(id);
        if (!order.status.equals("PAID") && !order.status.equals("SHIPPED")) {
            throw badRequest("only paid orders can be invoiced");
        }
        long number = ++INVOICE_COUNTER;
        bump("invoicesIssued");
        AUDIT.add("invoiced order " + order.id);

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", number);
        invoice.put("orderId", order.id);
        invoice.put("total", order.total);
        invoice.put("surcharge", order.surcharge);
        invoice.put("amountDue", order.total + order.surcharge);
        return invoice;
    }

    // ----- audit + metrics ------------------------------------------------

    @GetMapping("/audit")
    public List<String> audit() {
        return AUDIT;
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return METRICS;
    }

    // ----- helpers --------------------------------------------------------

    private OrderHeader requireOrder(Long id) {
        OrderHeader order = em.find(OrderHeader.class, id);
        if (order == null) {
            throw notFound("order");
        }
        return order;
    }

    private Coupon findCoupon(String code) {
        // the coupon code is the entity id, so a plain find is enough
        return em.find(Coupon.class, code);
    }

    private void bump(String metric) {
        METRICS.put(metric, METRICS.getOrDefault(metric, 0) + 1);
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    // ----- JPA entities (mixed into the controller on purpose) ------------

    @Entity
    @Table(name = "categories")
    public static class Category {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        public String name;
    }

    @Entity
    @Table(name = "customers")
    public static class Customer {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        public String name;
        public int loyaltyPoints;
    }

    @Entity
    @Table(name = "coupons")
    public static class Coupon {
        @Id
        public String code;   // the coupon code is its natural id
        public int percent;
        public int maxUses;
        public int timesUsed;
    }

    @Entity
    @Table(name = "products")
    public static class Product {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        public String name;
        public double price;
        public int stock;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "category_id")
        public Category category;
    }

    @Entity
    @Table(name = "orders")
    public static class OrderHeader {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        public Long customerId;
        public String status;
        public double total;
        public double surcharge;

        // sent in by the client, but not stored on the order
        @Transient
        public String couponCode;

        @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
        @JoinColumn(name = "order_id")
        public List<OrderLine> lines = new ArrayList<>();
    }

    @Entity
    @Table(name = "order_lines")
    public static class OrderLine {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        public Long productId;
        public int quantity;
        public double linePrice;
    }
}
