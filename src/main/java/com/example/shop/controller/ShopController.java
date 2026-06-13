package com.example.shop.controller;

import com.example.shop.model.*;
import com.example.shop.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
public class ShopController {

    private final CategoryService categoryService;
    private final CustomerService customerService;
    private final CouponService couponService;
    private final ProductService productService;
    private final OrderService orderService;
    private final AuditService auditService;

    public ShopController(CategoryService categoryService,
                          CustomerService customerService,
                          CouponService couponService,
                          ProductService productService,
                          OrderService orderService,
                          AuditService auditService) {
        this.categoryService = categoryService;
        this.customerService = customerService;
        this.couponService = couponService;
        this.productService = productService;
        this.orderService = orderService;
        this.auditService = auditService;
    }

    // ----- categories -----------------------------------------------------

    @PostMapping("/categories")
    public Category createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    @GetMapping("/categories/{id}")
    public Category getCategory(@PathVariable Long id) {
        return categoryService.getCategory(id);
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

    // ----- coupons --------------------------------------------------------

    @PostMapping("/coupons")
    public Coupon createCoupon(@RequestBody Coupon coupon) {
        return couponService.createCoupon(coupon);
    }

    // ----- products -------------------------------------------------------

    @PostMapping("/products")
    public Product createProduct(@RequestBody Map<String, Object> body) {
        return productService.createProduct(body);
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

    @PostMapping("/orders/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        return orderService.invoiceOrder(id);
    }

    // ----- audit + metrics ------------------------------------------------

    @GetMapping("/audit")
    public List<String> audit() {
        return auditService.getAuditLog();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return auditService.getMetrics();
    }
}