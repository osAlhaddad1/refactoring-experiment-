package com.example.shop.infrastructure;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {}

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository springRepository;

    public ProductRepositoryAdapter(SpringDataProductRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity jpaEntity = new ProductJpaEntity(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
        ProductJpaEntity saved = springRepository.save(jpaEntity);
        product.setId(saved.getId());
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return springRepository.findById(id)
                .map(jpa -> new Product(jpa.getId(), jpa.getName(), jpa.getPrice(), jpa.getStock()));
    }
}