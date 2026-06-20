package com.example.shop.application;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product();
        product.setId(null);
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        Product saved = productRepository.save(product);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product not found"));
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}
