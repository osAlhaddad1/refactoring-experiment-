package com.example.shop.controller;

import com.example.shop.model.Customer;
import com.example.shop.model.OrderHeader;
import com.example.shop.model.Product;
import com.example.shop.service.AuditService;
import com.example.shop.service.CustomerService;
import com.example.shop.service.OrderService;
import com.example.shop.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShopController {

    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;
    private final AuditService auditService;

    public ShopController(CustomerService customerService,
                          ProductService productService,
                          OrderService orderService,
                          AuditService auditService) {
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
        this.auditService = auditService;
    }

    // ----- customers ------------------------------------------------------

    @PostMapping("/customers")
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.createCustomer(customer);
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        return customerService.getCustomer(id);
    }

    // ----- products -------------------------------------------------------

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    // ----- orders ---------------------------------------------------------

    @PostMapping("/orders")
    public OrderHeader placeOrder(@RequestBody OrderHeader order) {
        return orderService.placeOrder(order);
    }

    @GetMapping("/orders/{id}")
    public OrderHeader getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderHeader payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderHeader shipOrder(@PathVariable Long id) {
        return orderService.shipOrder(id);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderHeader cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    // ----- audit ----------------------------------------------------------

    @GetMapping("/audit")
    public List<String> audit() {
        return auditService.getAuditLog();
    }
}