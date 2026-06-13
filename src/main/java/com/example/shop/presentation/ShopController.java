package com.example.shop.presentation;

import com.example.shop.application.OrderService;
import com.example.shop.application.OrderView;
import com.example.shop.application.ProductService;
import com.example.shop.application.ProductView;
import com.example.shop.application.ShopException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Presentation: talks to application services only, returns its own view types.
@RestController
public class ShopController {

    private final ProductService productService;
    private final OrderService orderService;

    public ShopController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @PostMapping("/products")
    public ProductView createProduct(@RequestBody ProductRequest request) {
        return productService.create(request.name, request.price, request.stock);
    }

    @GetMapping("/products/{id}")
    public ProductView getProduct(@PathVariable Long id) {
        return productService.get(id);
    }

    @GetMapping("/products")
    public List<ProductView> listProducts() {
        return productService.list();
    }

    @PostMapping("/orders")
    public OrderView placeOrder(@RequestBody OrderRequest request) {
        return orderService.place(request.productId, request.quantity);
    }

    @GetMapping("/orders/{id}")
    public OrderView getOrder(@PathVariable Long id) {
        return orderService.get(id);
    }

    @ExceptionHandler(ShopException.class)
    public ResponseEntity<String> handleShopError(ShopException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
