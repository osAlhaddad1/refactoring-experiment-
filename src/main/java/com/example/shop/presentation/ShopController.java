package com.example.shop.presentation;

import com.example.shop.application.InvalidOrderException;
import com.example.shop.application.OrderDto;
import com.example.shop.application.OrderNotFoundException;
import com.example.shop.application.OrderService;
import com.example.shop.application.ProductDto;
import com.example.shop.application.ProductNotFoundException;
import com.example.shop.application.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class ShopController {

    private final ProductService productService;
    private final OrderService orderService;

    public ShopController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto product) {
        return productService.createProduct(product);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        try {
            return productService.getProduct(id);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/products")
    public List<ProductDto> listProducts() {
        return productService.listProducts();
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto order) {
        try {
            return orderService.placeOrder(order);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidOrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        try {
            return orderService.getOrder(id);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}