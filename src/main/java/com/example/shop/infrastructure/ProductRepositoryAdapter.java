package com.example.shop.infrastructure;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Adapter: implements the domain port using JPA, maps domain <-> JPA entity.
@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpa;

    public ProductRepositoryAdapter(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    public Product save(Product product) {
        return toDomain(jpa.save(toEntity(product)));
    }

    public Optional<Product> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    public List<Product> findAll() {
        List<Product> result = new ArrayList<>();
        for (ProductEntity entity : jpa.findAll()) {
            result.add(toDomain(entity));
        }
        return result;
    }

    private ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.id = product.id;
        entity.name = product.name;
        entity.price = product.price;
        entity.stock = product.stock;
        return entity;
    }

    private Product toDomain(ProductEntity entity) {
        Product product = new Product();
        product.id = entity.id;
        product.name = entity.name;
        product.price = entity.price;
        product.stock = entity.stock;
        return product;
    }
}
