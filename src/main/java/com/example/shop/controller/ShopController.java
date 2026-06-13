package com.example.shop.controller;

import com.example.shop.model.Customer;
import com.example.shop.model.OrderHeader;
import com.example.shop.model.Product;
import com.example.shop.service.ShopService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/customers")
    public Customer createCustomer(@RequestBody Customer customer) {
        return shopService.createCustomer(customer);
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        return shopService.getCustomer(id);
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return shopService.createProduct(product);
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return shopService.getProduct(id);
    }

    @PostMapping("/orders")
    public OrderHeader placeOrder(@RequestBody OrderHeader order) {
        return shopService.placeOrder(order);
    }

    @GetMapping("/orders/{id}")
    public OrderHeader getOrder(@PathVariable Long id) {
        return shopService.getOrder(id);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderHeader payOrder(@PathVariable Long id) {
        return shopService.payOrder(id);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderHeader shipOrder(@PathVariable Long id) {
        return shopService.shipOrder(id);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderHeader cancelOrder(@PathVariable Long id) {
        return shopService.cancelOrder(id);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLogs();
    }
}