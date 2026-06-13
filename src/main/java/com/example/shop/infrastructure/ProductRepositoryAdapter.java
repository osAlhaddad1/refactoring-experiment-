package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Product save(Product product) {
        ProductEntity entity;
        if (product.id == null) {
            entity = new ProductEntity();
            entity.name = product.name;
            entity.price = product.price;
            entity.stock = product.stock;
            if (product.category != null) {
                entity.category = em.find(CategoryEntity.class, product.category.id);
            }
            em.persist(entity);
        } else {
            entity = em.find(ProductEntity.class, product.id);
            if (entity != null) {
                entity.name = product.name;
                entity.price = product.price;
                entity.stock = product.stock;
                if (product.category != null) {
                    entity.category = em.find(CategoryEntity.class, product.category.id);
                } else {
                    entity.category = null;
                }
                entity = em.merge(entity);
            }
        }
        product.id = entity.id;
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        ProductEntity entity = em.find(ProductEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        Category category = null;
        if (entity.category != null) {
            category = new Category(entity.category.id, entity.category.name);
        }
        return Optional.of(new Product(entity.id, entity.name, entity.price, entity.stock, category));
    }
}
