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
        CategoryEntity entity = new CategoryEntity();
        entity.id = category.id;
        entity.name = category.name;
        CategoryEntity saved = repository.save(entity);
        category.id = saved.id;
        return category;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return repository.findById(id).map(entity -> {
            Category category = new Category();
            category.id = entity.id;
            category.name = entity.name;
            return category;
        });
    }
}
