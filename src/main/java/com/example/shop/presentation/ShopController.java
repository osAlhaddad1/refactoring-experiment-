package com.example.shop.presentation;

import com.example.shop.application.ShopService;
import com.example.shop.application.ProductData;
import com.example.shop.application.OrderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
        ProductData data = shopService.createProduct(request.name, request.price, request.stock);
        return new ProductResponse(data.id, data.name, data.price, data.stock);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        ProductData data = shopService.getProduct(id);
        return new ProductResponse(data.id, data.name, data.price, data.stock);
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return shopService.listProducts().stream()
                .map(data -> new ProductResponse(data.id, data.name, data.price, data.stock))
                .collect(Collectors.toList());
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        OrderData data = shopService.placeOrder(request.productId, request.quantity);
        return new OrderResponse(data.id, data.productId, data.quantity, data.total);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        OrderData data = shopService.getOrder(id);
        return new OrderResponse(data.id, data.productId, data.quantity, data.total);
    }
}
