package com.example.shop.presentation;

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

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto dto) {
        return shopService.createProduct(dto);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return shopService.getProduct(id);
    }

    @GetMapping("/products")
    public List<ProductDto> listProducts() {
        return shopService.listProducts();
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto dto) {
        return shopService.placeOrder(dto);
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return shopService.getOrder(id);
    }
}