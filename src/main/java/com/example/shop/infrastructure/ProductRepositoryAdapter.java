package com.example.shop.infrastructure;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {
    private final ProductJpaRepository repository;

    public ProductRepositoryAdapter(ProductJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.id = product.id;
        entity.name = product.name;
        entity.price = product.price;
        entity.stock = product.stock;
        
        ProductEntity saved = repository.save(entity);
        
        product.id = saved.id;
        product.name = saved.name;
        product.price = saved.price;
        product.stock = saved.stock;
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(entity -> {
            Product product = new Product();
            product.id = entity.id;
            product.name = entity.name;
            product.price = entity.price;
            product.stock = entity.stock;
            return product;
        });
    }
}