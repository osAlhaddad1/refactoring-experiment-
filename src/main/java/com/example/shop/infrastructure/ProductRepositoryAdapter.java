package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final SpringProductRepository springRepository;

    public ProductRepositoryAdapter(SpringProductRepository springRepository) {
        this.springRepository = springRepository;
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
        ProductEntity saved = springRepository.save(entity);
        Category category = null;
        if (saved.category != null) {
            category = new Category(saved.category.id, saved.category.name);
        }
        return new Product(saved.id, saved.name, saved.price, saved.stock, category);
    }

    @Override
    public Product findById(Long id) {
        return springRepository.findById(id)
                .map(entity -> {
                    Category category = null;
                    if (entity.category != null) {
                        category = new Category(entity.category.id, entity.category.name);
                    }
                    return new Product(entity.id, entity.name, entity.price, entity.stock, category);
                })
                .orElse(null);
    }
}