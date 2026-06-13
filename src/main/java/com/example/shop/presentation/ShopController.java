package com.example.shop.presentation;

import com.example.shop.application.BadRequestException;
import com.example.shop.application.NotFoundException;
import com.example.shop.application.OrderDto;
import com.example.shop.application.OrderUseCase;
import com.example.shop.application.ProductDto;
import com.example.shop.application.ProductUseCase;
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

    private final ProductUseCase productUseCase;
    private final OrderUseCase orderUseCase;

    public ShopController(ProductUseCase productUseCase, OrderUseCase orderUseCase) {
        this.productUseCase = productUseCase;
        this.orderUseCase = orderUseCase;
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        ProductDto dto = new ProductDto(null, request.getName(), request.getPrice(), request.getStock());
        ProductDto saved = productUseCase.createProduct(dto);
        return ProductResponse.fromDto(saved);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        try {
            ProductDto dto = productUseCase.getProduct(id);
            return ProductResponse.fromDto(dto);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return productUseCase.listProducts().stream()
                .map(ProductResponse::fromDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderDto dto = new OrderDto(null, request.getProductId(), request.getQuantity(), 0.0);
            OrderDto saved = orderUseCase.placeOrder(dto);
            return OrderResponse.fromDto(saved);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        try {
            OrderDto dto = orderUseCase.getOrder(id);
            return OrderResponse.fromDto(dto);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
