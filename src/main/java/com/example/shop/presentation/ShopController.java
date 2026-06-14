package com.example.shop.presentation;

import com.example.shop.application.ProductDto;
import com.example.shop.application.OrderDto;
import com.example.shop.application.ProductService;
import com.example.shop.application.OrderService;
import com.example.shop.application.ResourceNotFoundException;
import com.example.shop.application.InvalidOrderException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {

    private final ProductService productService;
    private final OrderService orderService;

    public ShopController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        ProductDto dto = productService.createProduct(request.name, request.price, request.stock);
        return new ProductResponse(dto.id, dto.name, dto.price, dto.stock);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        try {
            ProductDto dto = productService.getProduct(id);
            return new ProductResponse(dto.id, dto.name, dto.price, dto.stock);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return productService.listProducts().stream()
                .map(dto -> new ProductResponse(dto.id, dto.name, dto.price, dto.stock))
                .collect(Collectors.toList());
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderDto dto = orderService.placeOrder(request.productId, request.quantity);
            return new OrderResponse(dto.id, dto.productId, dto.quantity, dto.total);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidOrderException e) { 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        try {
            OrderDto dto = orderService.getOrder(id);
            return new OrderResponse(dto.id, dto.productId, dto.quantity, dto.total);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
