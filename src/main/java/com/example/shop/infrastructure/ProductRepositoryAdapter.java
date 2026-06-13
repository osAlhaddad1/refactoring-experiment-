package com.example.shop.infrastructure;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {
    private final SpringDataProductRepository repository;

    public ProductRepositoryAdapter(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.id = product.getId();
        entity.name = product.getName();
        entity.price = product.getPrice();
        entity.stock = product.getStock();

        ProductEntity saved = repository.save(entity);

        product.setId(saved.id);
        product.setName(saved.name);
        product.setPrice(saved.price);
        product.setStock(saved.stock);
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(entity -> {
            Product product = new Product();
            product.setId(entity.id);
            product.setName(entity.name);
            product.setPrice(entity.price);
            product.setStock(entity.stock);
            return product;
        });
    }
}
