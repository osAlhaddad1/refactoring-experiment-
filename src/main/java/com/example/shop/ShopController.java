package com.example.shop;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * SIMPLE BASELINE "god file".
 *
 * Everything is mixed into one class in the root package: HTTP handling, the
 * business rules (bulk discount + stock management) and raw JPA persistence.
 * It works -- the HTTP behaviour tests pass -- but it has no architecture, so
 * the ArchUnit gate fails on it (a *Controller is not allowed to live in the
 * root package; it should be in ..presentation..).
 *
 * The JPA entities are deliberately nested inside the controller to underline
 * that there are no layers here at all.
 */
@RestController
public class ShopController {

    @PersistenceContext
    private EntityManager em;

    // ----- product endpoints ----------------------------------------------

    @PostMapping("/products")
    @Transactional
    public Product createProduct(@RequestBody Product product) {
        product.id = null; // the server assigns the id
        em.persist(product);
        return product;
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        Product product = em.find(Product.class, id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
        return product;
    }

    @GetMapping("/products")
    public List<Product> listProducts() {
        return em.createQuery("select p from Product p", Product.class).getResultList();
    }

    // ----- order endpoints ------------------------------------------------

    @PostMapping("/orders")
    @Transactional
    public OrderRecord placeOrder(@RequestBody OrderRecord order) {
        Product product = em.find(Product.class, order.productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
        if (order.quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }
        if (product.stock < order.quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
        }

        // business rule: buy 10 or more and you get 10% off the whole line
        double total = product.price * order.quantity;
        if (order.quantity >= 10) {
            total = total * 0.9;
        }

        // stock management: take the items out of stock
        product.stock = product.stock - order.quantity;

        order.id = null;
        order.total = total;
        em.persist(order);
        return order;
    }

    @GetMapping("/orders/{id}")
    public OrderRecord getOrder(@PathVariable Long id) {
        OrderRecord order = em.find(OrderRecord.class, id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found");
        }
        return order;
    }

    // ----- JPA entities (mixed into the controller on purpose) ------------

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
    public static class OrderRecord {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        public Long productId;
        public int quantity;
        public double total;
    }
}
