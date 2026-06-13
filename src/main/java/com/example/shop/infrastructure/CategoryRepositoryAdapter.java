package com.example.shop.infrastructure;

import com.example.shop.domain.Category;
import com.example.shop.domain.CategoryRepository;
import org.springframework.stereotype.Component;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final SpringCategoryRepository springRepository;

    public CategoryRepositoryAdapter(SpringCategoryRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.id = category.id;
        entity.name = category.name;
        CategoryEntity saved = springRepository.save(entity);
        return new Category(saved.id, saved.name);
    }

    @Override
    public Category findById(Long id) {
        return springRepository.findById(id)
                .map(entity -> new Category(entity.id, entity.name))
                .orElse(null);
    }
}