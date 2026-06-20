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
        ProductJpaEntity entity = new ProductJpaEntity(product.getId(), product.getName(), product.getPrice(), product.getStock());
        if (entity.getId() == null) {
            em.persist(entity);
            em.flush();
            product.setId(entity.getId());
        } else {
            entity = em.merge(entity);
            em.flush();
        }
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        ProductJpaEntity entity = em.find(ProductJpaEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.getStock()));
    }
}