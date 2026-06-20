package com.example.shop.presentation;

import com.example.shop.domain.OrderRecord;
import com.example.shop.domain.Product;
import com.example.shop.service.ShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return shopService.createProduct(product);
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return shopService.getProduct(id);
    }

    @GetMapping("/products")
    public List<Product> listProducts() {
        return shopService.listProducts();
    }

    @PostMapping("/orders")
    public OrderRecord placeOrder(@RequestBody OrderRecord order) {
        return shopService.placeOrder(order);
    }

    @GetMapping("/orders/{id}")
    public OrderRecord getOrder(@PathVariable Long id) {
        return shopService.getOrder(id);
    }
}