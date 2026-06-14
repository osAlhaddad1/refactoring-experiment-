package com.example.shop.presentation;

import com.example.shop.application.InvalidOrderException;
import com.example.shop.application.OrderDto;
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
        ProductDto inputDto = new ProductDto(null, request.getName(), request.getPrice(), request.getStock());
        ProductDto outputDto = productService.createProduct(inputDto);
        return new ProductResponse(outputDto.getId(), outputDto.getName(), outputDto.getPrice(), outputDto.getStock());
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        ProductDto outputDto = productService.getProduct(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return new ProductResponse(outputDto.getId(), outputDto.getName(), outputDto.getPrice(), outputDto.getStock());
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return productService.listProducts().stream()
                .map(dto -> new ProductResponse(dto.getId(), dto.getName(), dto.getPrice(), dto.getStock()))
                .collect(Collectors.toList());
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderDto inputDto = new OrderDto(null, request.getProductId(), request.getQuantity(), 0.0);
            OrderDto outputDto = orderService.placeOrder(inputDto);
            return new OrderResponse(outputDto.getId(), outputDto.getProductId(), outputDto.getQuantity(), outputDto.getTotal());
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidOrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        OrderDto outputDto = orderService.getOrder(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return new OrderResponse(outputDto.getId(), outputDto.getProductId(), outputDto.getQuantity(), outputDto.getTotal());
    }
}
