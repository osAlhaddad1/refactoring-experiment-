package com.example.shop.infrastructure;

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
        if (product.getId() == null) {
            entity = new ProductEntity();
        } else {
            entity = em.find(ProductEntity.class, product.getId());
            if (entity == null) {
                entity = new ProductEntity();
            }
        }
        entity.name = product.getName();
        entity.price = product.getPrice();
        entity.stock = product.getStock();

        if (entity.id == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        return toDomain(entity);
    }

    @Override
    public Optional<Product> findById(Long id) {
        ProductEntity entity = em.find(ProductEntity.class, id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    private Product toDomain(ProductEntity entity) {
        Product product = new Product();
        product.setId(entity.id);
        product.setName(entity.name);
        product.setPrice(entity.price);
        product.setStock(entity.stock);
        return product;
    }
}
