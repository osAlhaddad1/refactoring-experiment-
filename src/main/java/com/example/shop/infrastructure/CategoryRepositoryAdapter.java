package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.CategoryRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final SpringDataCategoryRepository repository;

    public CategoryRepositoryAdapter(SpringDataCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity;
        if (category.getId() != null) {
            entity = repository.findById(category.getId()).orElseGet(CategoryEntity::new);
        } else {
            entity = new CategoryEntity();
        }
        entity.setName(category.getName());
        CategoryEntity saved = repository.save(entity);
        category.setId(saved.getId());
        return category;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return repository.findById(id).map(entity -> new Category(entity.getId(), entity.getName()));
    }
}
