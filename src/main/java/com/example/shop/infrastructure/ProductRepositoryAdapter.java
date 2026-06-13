package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
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
        ProductEntity entity;
        if (product.getId() != null) {
            entity = repository.findById(product.getId()).orElseGet(ProductEntity::new);
        } else {
            entity = new ProductEntity();
        }
        entity.setName(product.getName());
        entity.setPrice(product.getPrice());
        entity.setStock(product.getStock());
        if (product.getCategory() != null) {
            CategoryEntity catEntity = new CategoryEntity();
            catEntity.setId(product.getCategory().getId());
            catEntity.setName(product.getCategory().getName());
            entity.setCategory(catEntity);
        } else {
            entity.setCategory(null);
        }
        ProductEntity saved = repository.save(entity);
        product.setId(saved.getId());
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(entity -> {
            Category category = null;
            if (entity.getCategory() != null) {
                category = new Category(entity.getCategory().getId(), entity.getCategory().getName());
            }
            return new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.getStock(), category);
        });
    }
}
