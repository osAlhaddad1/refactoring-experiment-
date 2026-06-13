package com.example.shop;

import jakarta.persistence.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * COMPLEX BASELINE "god file".
 *
 * One class in the root package that mixes everything together: HTTP handling,
 * a multi-step pricing engine, an order state machine (pay / ship / cancel /
 * restock), customer loyalty points, a JPA one-to-many model (Order ->
 * OrderLine), and a global static audit log.
 *
 * It works (the HTTP tests pass) but it has no architecture, so the ArchUnit
 * gate fails on it (a *Controller is not allowed in the root package).
 */
@RestController
public class ShopController {

    // Global static state: a shared, mutable audit log (the anti-pattern that a
    // refactoring has to hide behind a port).
    static final List<String> AUDIT = new ArrayList<>();

    @PersistenceContext
    private EntityManager em;

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

    // ----- products -------------------------------------------------------

    @PostMapping("/products")
    @Transactional
    public Product createProduct(@RequestBody Product product) {
        product.id = null;
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
                linePrice = linePrice * 90 / 100;   // *90/100 keeps whole-number prices exact
            }
            line.linePrice = linePrice;
            subtotal = subtotal + linePrice;

            // reserve the stock
            product.stock = product.stock - line.quantity;
        }
        // step 2: an order-level discount for big orders (subtotal 500+ -> 5% off)
        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        order.id = null;
        order.status = "NEW";
        order.total = total;
        em.persist(order);
        AUDIT.add("created order " + order.id);
        return order;
    }

    @GetMapping("/orders/{id}")
    public OrderHeader getOrder(@PathVariable Long id) {
        OrderHeader order = em.find(OrderHeader.class, id);
        if (order == null) {
            throw notFound("order");
        }
        return order;
    }

    @PostMapping("/orders/{id}/pay")
    @Transactional
    public OrderHeader payOrder(@PathVariable Long id) {
        OrderHeader order = requireOrder(id);
        if (!order.status.equals("NEW")) {
            throw badRequest("only NEW orders can be paid");
        }
        order.status = "PAID";
        // loyalty: earn 1 point per whole currency unit of the order total
        Customer customer = em.find(Customer.class, order.customerId);
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
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
        // restock: put the items back into stock
        for (OrderLine line : order.lines) {
            Product product = em.find(Product.class, line.productId);
            if (product != null) {
                product.stock = product.stock + line.quantity;
            }
        }
        order.status = "CANCELLED";
        AUDIT.add("cancelled order " + order.id);
        return order;
    }

    // ----- audit ----------------------------------------------------------

    @GetMapping("/audit")
    public List<String> audit() {
        return AUDIT;
    }

    // ----- helpers --------------------------------------------------------

    private OrderHeader requireOrder(Long id) {
        OrderHeader order = em.find(OrderHeader.class, id);
        if (order == null) {
            throw notFound("order");
        }
        return order;
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    // ----- JPA entities (mixed into the controller on purpose) ------------

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
    @Table(name = "products")
    public static class Product {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        public String name;
        public double price;
        public int stock;
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
