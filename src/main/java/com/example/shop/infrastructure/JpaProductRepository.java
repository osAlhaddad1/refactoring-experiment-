package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class JpaProductRepository implements ProductRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Product save(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.id = product.getId();
        entity.name = product.getName();
        entity.price = product.getPrice();
        entity.stock = product.getStock();
        if (product.getCategory() != null) {
            CategoryEntity catEntity = em.find(CategoryEntity.class, product.getCategory().getId());
            entity.category = catEntity;
        }
        if (entity.id == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        product.setId(entity.id);
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