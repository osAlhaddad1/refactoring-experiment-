package com.example.shop.presentation;

import com.example.shop.application.ShopApplicationService;
import com.example.shop.application.ProductData;
import com.example.shop.application.OrderData;
import com.example.shop.application.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {

    private final ShopApplicationService shopApplicationService;

    public ShopController(ShopApplicationService shopApplicationService) {
        this.shopApplicationService = shopApplicationService;
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto productDto) {
        ProductData input = new ProductData(null, productDto.name, productDto.price, productDto.stock);
        ProductData output = shopApplicationService.createProduct(input);
        return ProductDto.fromData(output);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        ProductData output = shopApplicationService.getProduct(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return ProductDto.fromData(output);
    }

    @GetMapping("/products")
    public List<ProductDto> listProducts() {
        return shopApplicationService.listProducts().stream()
                .map(ProductDto::fromData)
                .collect(Collectors.toList());
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto orderDto) {
        try {
            OrderData output = shopApplicationService.placeOrder(orderDto.productId, orderDto.quantity);
            return OrderDto.fromData(output);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } 
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        OrderData output = shopApplicationService.getOrder(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return OrderDto.fromData(output);
    }
}