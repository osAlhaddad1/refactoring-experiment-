package com.example.shop.application;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product(null, dto.getName(), dto.getPrice(), dto.getStock());
        Product saved = productRepository.save(product);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    public Optional<ProductDto> getProduct(Long id) {
        return productRepository.findById(id)
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()));
    }

    public List<ProductDto> listProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()))
                .collect(Collectors.toList());
    }
}
