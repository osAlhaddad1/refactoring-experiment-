package com.example.shop.presentation;

import com.example.shop.application.ProductDto;
import com.example.shop.application.OrderDto;
import com.example.shop.application.ShopService;
import com.example.shop.application.ProductNotFoundException;
import com.example.shop.application.OrderNotFoundException;
import com.example.shop.application.InvalidQuantityException;
import com.example.shop.application.InsufficientStockException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        ProductDto dto = new ProductDto(null, request.name, request.price, request.stock);
        ProductDto saved = shopService.createProduct(dto);
        return new ProductResponse(saved.id, saved.name, saved.price, saved.stock);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        try {
            ProductDto saved = shopService.getProduct(id);
            return new ProductResponse(saved.id, saved.name, saved.price, saved.stock);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return shopService.listProducts().stream()
                .map(saved -> new ProductResponse(saved.id, saved.name, saved.price, saved.stock))
                .collect(Collectors.toList());
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderDto dto = new OrderDto();
            dto.productId = request.productId;
            dto.quantity = request.quantity;
            OrderDto saved = shopService.placeOrder(dto);
            return new OrderResponse(saved.id, saved.productId, saved.quantity, saved.total);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidQuantityException | InsufficientStockException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        try {
            OrderDto saved = shopService.getOrder(id);
            return new OrderResponse(saved.id, saved.productId, saved.quantity, saved.total);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
