package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Category save(Category category) {
        CategoryEntity entity;
        if (category.id == null) {
            entity = new CategoryEntity();
            entity.name = category.name;
            em.persist(entity);
        } else {
            entity = em.find(CategoryEntity.class, category.id);
            if (entity != null) {
                entity.name = category.name;
                entity = em.merge(entity);
            }
        }
        category.id = entity.id;
        return category;
    }

    @Override
    public Optional<Category> findById(Long id) {
        CategoryEntity entity = em.find(CategoryEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new Category(entity.id, entity.name));
    }
}
