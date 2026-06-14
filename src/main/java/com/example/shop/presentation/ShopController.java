package com.example.shop.presentation;

import com.example.shop.application.ProductDto;

import com.example.shop.application.OrderDto;
import com.example.shop.application.ShopService;
import com.example.shop.application.ProductNotFoundException;
import com.example.shop.application.InvalidQuantityException;
import com.example.shop.application.InsufficientStockException;
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

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto productDto) {
        return shopService.createProduct(productDto);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return shopService.getProduct(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }

    @GetMapping("/products")
    public List<ProductDto> listProducts() {
        return shopService.listProducts();
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            return shopService.placeOrder(orderRequest.getProductId(), orderRequest.getQuantity());
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        } catch (InvalidQuantityException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        } catch (InsufficientStockException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
        }
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return shopService.getOrder(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }
}