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
        entity.setId(category.getId());
        entity.setName(category.getName());
        CategoryEntity saved = repository.save(entity);
        return new Category(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Category> findById(Long id) {
        return repository.findById(id).map(entity -> new Category(entity.getId(), entity.getName()));
    }
}
