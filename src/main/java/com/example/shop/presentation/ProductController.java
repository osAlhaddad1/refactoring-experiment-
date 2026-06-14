package com.example.shop.presentation;

import com.example.shop.application.BadRequestException;
import com.example.shop.application.NotFoundException;
import com.example.shop.application.ProductDto;
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
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        try {
            ProductDto dto = productService.createProduct(request.name, request.price, request.stock);
            return new ProductResponse(dto.id, dto.name, dto.price, dto.stock);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        try {
            ProductDto dto = productService.getProduct(id);
            return new ProductResponse(dto.id, dto.name, dto.price, dto.stock);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return productService.listProducts().stream()
                .map(dto -> new ProductResponse(dto.id, dto.name, dto.price, dto.stock))
                .collect(Collectors.toList());
    }
}