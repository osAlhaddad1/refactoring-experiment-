package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaRepository;

    public ProductRepositoryAdapter(JpaProductRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = toEntity(product);
        ProductEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private ProductEntity toEntity(Product domain) {
        if (domain == null) return null;
        ProductEntity entity = new ProductEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setPrice(domain.getPrice());
        entity.setStock(domain.getStock());
        if (domain.getCategory() != null) {
            CategoryEntity catEntity = new CategoryEntity();
            catEntity.setId(domain.getCategory().getId());
            catEntity.setName(domain.getCategory().getName());
            entity.setCategory(catEntity);
        }
        return entity;
    }

    private Product toDomain(ProductEntity entity) {
        if (entity == null) return null;
        Category category = null;
        if (entity.getCategory() != null) {
            category = new Category(entity.getCategory().getId(), entity.getCategory().getName());
        }
        return new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.getStock(), category);
    }
}