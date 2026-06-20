package com.example.shop.presentation;

import com.example.shop.application.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/customers")
    public CustomerResponse createCustomer(@RequestBody CustomerRequest request) {
        CustomerDto dto = new CustomerDto();
        dto.name = request.name;
        CustomerDto saved = shopService.createCustomer(dto);
        return toResponse(saved);
    }

    @GetMapping("/customers/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        CustomerDto dto = shopService.getCustomer(id);
        return toResponse(dto);
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        ProductDto dto = new ProductDto();
        dto.name = request.name;
        dto.price = request.price;
        dto.stock = request.stock;
        ProductDto saved = shopService.createProduct(dto);
        return toResponse(saved);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        ProductDto dto = shopService.getProduct(id);
        return toResponse(dto);
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        OrderDto dto = new OrderDto();
        dto.customerId = request.customerId;
        if (request.lines != null) {
            dto.lines = request.lines.stream().map(line -> {
                OrderLineDto lineDto = new OrderLineDto();
                lineDto.productId = line.productId;
                lineDto.quantity = line.quantity;
                return lineDto;
            }).collect(Collectors.toList());
        }
        OrderDto saved = shopService.placeOrder(dto);
        return toResponse(saved);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        OrderDto dto = shopService.getOrder(id);
        return toResponse(dto);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderResponse payOrder(@PathVariable Long id) {
        OrderDto dto = shopService.payOrder(id);
        return toResponse(dto);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderResponse shipOrder(@PathVariable Long id) {
        OrderDto dto = shopService.shipOrder(id);
        return toResponse(dto);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        OrderDto dto = shopService.cancelOrder(id);
        return toResponse(dto);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLogs();
    }

    private CustomerResponse toResponse(CustomerDto dto) {
        CustomerResponse response = new CustomerResponse();
        response.id = dto.id;
        response.name = dto.name;
        response.loyaltyPoints = dto.loyaltyPoints;
        return response;
    }

    private ProductResponse toResponse(ProductDto dto) {
        ProductResponse response = new ProductResponse();
        response.id = dto.id;
        response.name = dto.name;
        response.price = dto.price;
        response.stock = dto.stock;
        return response;
    }

    private OrderResponse toResponse(OrderDto dto) {
        OrderResponse response = new OrderResponse();
        response.id = dto.id;
        response.customerId = dto.customerId;
        response.status = dto.status;
        response.total = dto.total;
        if (dto.lines != null) {
            response.lines = dto.lines.stream().map(line -> {
                OrderLineResponse lineResponse = new OrderLineResponse();
                lineResponse.id = line.id;
                lineResponse.productId = line.productId;
                lineResponse.quantity = line.quantity;
                lineResponse.linePrice = line.linePrice;
                return lineResponse;
            }).collect(Collectors.toList());
        }
        return response;
    }
}