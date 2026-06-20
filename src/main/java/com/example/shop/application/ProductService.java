package com.example.shop.application;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepositoryPort productRepository;

    public ProductService(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product(null, dto.name, dto.price, dto.stock);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));
        return toDto(product);
    }

    public List<ProductDto> listProducts() {
        return productRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}
