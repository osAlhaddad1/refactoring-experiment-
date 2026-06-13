package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class JpaCategoryRepository implements CategoryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Category save(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.id = category.getId();
        entity.name = category.getName();
        if (entity.id == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        category.setId(entity.id);
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