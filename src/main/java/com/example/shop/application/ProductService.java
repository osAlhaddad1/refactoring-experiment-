package com.example.shop.application;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductDto createProduct(String name, double price, int stock) {
        Product product = new Product(null, name, price, stock);
        Product saved = productRepository.save(product);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }

    public List<ProductDto> listProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()))
                .collect(Collectors.toList());
    }
}
