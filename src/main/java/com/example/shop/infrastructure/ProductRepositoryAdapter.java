package com.example.shop.infrastructure;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductRepositoryAdapter implements ProductRepository {
    private final SpringDataProductRepository repository;

    public ProductRepositoryAdapter(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = new ProductEntity(product.getId(), product.getName(), product.getPrice(), product.getStock());
        ProductEntity saved = repository.save(entity);
        return new Product(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id)
                .map(entity -> new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.getStock()));
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll().stream()
                .map(entity -> new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.getStock()))
                .collect(Collectors.toList());
    }
}
