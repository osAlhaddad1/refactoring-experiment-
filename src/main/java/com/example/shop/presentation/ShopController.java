package com.example.shop.presentation;

import com.example.shop.application.CustomerDto;
import com.example.shop.application.OrderDto;
import com.example.shop.application.ProductDto;
import com.example.shop.application.ShopService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/customers")
    public CustomerDto createCustomer(@RequestBody CustomerDto customer) {
        return shopService.createCustomer(customer);
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        return shopService.getCustomer(id);
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto product) {
        return shopService.createProduct(product);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return shopService.getProduct(id);
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto order) {
        return shopService.placeOrder(order);
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return shopService.getOrder(id);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderDto payOrder(@PathVariable Long id) {
        return shopService.payOrder(id);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderDto shipOrder(@PathVariable Long id) {
        return shopService.shipOrder(id);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderDto cancelOrder(@PathVariable Long id) {
        return shopService.cancelOrder(id);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLogs();
    }
}
