package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProductAdapter implements ProductRepository {
    private final SpringDataProductRepository repository;

    public ProductAdapter(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.id = product.id;
        entity.name = product.name;
        entity.price = product.price;
        entity.stock = product.stock;
        if (product.category != null) {
            CategoryEntity catEntity = new CategoryEntity();
            catEntity.id = product.category.id;
            catEntity.name = product.category.name;
            entity.category = catEntity;
        }
        entity = repository.save(entity);
        product.id = entity.id;
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
            if (entity.category != null) {
                Category category = new Category();
                category.id = entity.category.id;
                category.name = entity.category.name;
                product.category = category;
            }
            return product;
        });
    }
}
