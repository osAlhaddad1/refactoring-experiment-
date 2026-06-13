package com.example.shop.presentation;

import com.example.shop.application.ShopApplicationService;
import com.example.shop.domain.Order;
import com.example.shop.domain.Product;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {

    private final ShopApplicationService shopService;

    public ShopController(ShopApplicationService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto dto) {
        Product product = shopService.createProduct(dto.name, dto.price, dto.stock);
        return toDto(product);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        Product product = shopService.getProduct(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return toDto(product);
    }

    @GetMapping("/products")
    public List<ProductDto> listProducts() {
        return shopService.listProducts().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto dto) {
        try {
            Order order = shopService.placeOrder(dto.productId, dto.quantity);
            return toDto(order);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (com.example.shop.application.ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        Order order = shopService.getOrder(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return toDto(order);
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.price = product.getPrice();
        dto.stock = product.getStock();
        return dto;
    }

    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.id = order.getId();
        dto.productId = order.getProductId();
        dto.quantity = order.getQuantity();
        dto.total = order.getTotal();
        return dto;
    }
}
