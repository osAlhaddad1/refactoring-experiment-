package com.example.shop.presentation;

import com.example.shop.application.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
        ProductDto dto = productService.createProduct(request.getName(), request.getPrice(), request.getStock());
        return ProductResponse.fromDto(dto);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        try {
            ProductDto dto = productService.getProduct(id);
            return ProductResponse.fromDto(dto);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return productService.listProducts().stream()
                .map(ProductResponse::fromDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderDto dto = orderService.placeOrder(request.getProductId(), request.getQuantity());
            return OrderResponse.fromDto(dto);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    } catch (InvalidQuantityException | InsufficientStockException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        try {
            OrderDto dto = orderService.getOrder(id);
            return OrderResponse.fromDto(dto);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
