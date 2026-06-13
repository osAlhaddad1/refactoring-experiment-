package com.example.shop.presentation;

import com.example.shop.application.OrderDTO;
import com.example.shop.application.ProductDTO;
import com.example.shop.application.ProductService;
import com.example.shop.application.OrderService;
import com.example.shop.application.ProductNotFoundException;
import com.example.shop.application.OrderNotFoundException;
import com.example.shop.application.InvalidOrderException;
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

    private final ProductService productService;
    private final OrderService orderService;

    public ShopController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @PostMapping("/products")
    public ProductDTO createProduct(@RequestBody ProductDTO product) {
        return productService.createProduct(product);
    }

    @GetMapping("/products/{id}")
    public ProductDTO getProduct(@PathVariable Long id) {
        try {
            return productService.getProduct(id);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
    }

    @GetMapping("/products")
    public List<ProductDTO> listProducts() {
        return productService.listProducts();
    }

    @PostMapping("/orders")
    public OrderDTO placeOrder(@RequestBody OrderDTO order) {
        try {
            return orderService.placeOrder(order);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
    } catch (InvalidOrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderDTO getOrder(@PathVariable Long id) {
        try {
            return orderService.getOrder(id);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found");
        }
    }
}
