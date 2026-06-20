package com.example.shop.infrastructure;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {
    private final ProductJpaRepository repository;

    public ProductRepositoryAdapter(ProductJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.id = product.getId();
        entity.name = product.getName();
        entity.price = product.getPrice();
        entity.stock = product.getStock();
        ProductJpaEntity saved = repository.save(entity);
        
        Product savedProduct = new Product();
        savedProduct.setId(saved.id);
        savedProduct.setName(saved.name);
        savedProduct.setPrice(saved.price);
        savedProduct.setStock(saved.stock);
        return savedProduct;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(entity -> {
            Product product = new Product();
            product.setId(entity.id);
            product.setName(entity.name);
            product.setPrice(entity.price);
            product.setStock(entity.stock);
            return product;
        });
    }
}