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
    public ProductInfo createProduct(String name, double price, int stock) {
        Product product = new Product(null, name, price, stock);
        Product saved = productRepository.save(product);
        return new ProductInfo(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    public ProductInfo getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("product"));
        return new ProductInfo(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}