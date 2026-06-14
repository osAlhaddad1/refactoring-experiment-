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
        ProductEntity entity = EntityMapper.toEntity(product);
        ProductEntity saved = repository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(EntityMapper::toDomain);
    }
}
