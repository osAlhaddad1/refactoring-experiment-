package com.example.shop.infrastructure;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {
    private final SpringDataProductRepository repository;

    public ProductRepositoryAdapter(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setPrice(product.getPrice());
        entity.setStock(product.getStock());
        ProductEntity saved = repository.save(entity);
        return new Product(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(entity -> new Product(
            entity.getId(),
            entity.getName(),
            entity.getPrice(),
            entity.getStock()
        ));
    }
}