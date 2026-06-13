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
        ProductEntity entity = new ProductEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setPrice(product.getPrice());
        entity.setStock(product.getStock());
        if (product.getCategory() != null) {
            CategoryEntity catEntity = new CategoryEntity();
            catEntity.setId(product.getCategory().getId());
            catEntity.setName(product.getCategory().getName());
            entity.setCategory(catEntity);
        }
        ProductEntity saved = repository.save(entity);
        Category cat = null;
        if (saved.getCategory() != null) {
            cat = new Category(saved.getCategory().getId(), saved.getCategory().getName());
        }
        return new Product(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock(), cat);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(entity -> {
            Category cat = null;
            if (entity.getCategory() != null) {
                cat = new Category(entity.getCategory().getId(), entity.getCategory().getName());
            }
            return new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.getStock(), cat);
        });
    }
}
