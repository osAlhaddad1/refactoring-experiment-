package com.example.shop.infrastructure;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Product save(Product product) {
        ProductJpaEntity jpaEntity = toJpa(product);
        if (jpaEntity.getId() == null) {
            em.persist(jpaEntity);
        } else {
            jpaEntity = em.merge(jpaEntity);
        }
        return toDomain(jpaEntity);
    }

    @Override
    public Optional<Product> findById(Long id) {
        ProductJpaEntity jpaEntity = em.find(ProductJpaEntity.class, id);
        return Optional.ofNullable(jpaEntity).map(this::toDomain);
    }

    @Override
    public List<Product> findAll() {
        List<ProductJpaEntity> entities = em.createQuery("select p from ProductJpaEntity p", ProductJpaEntity.class)
                .getResultList();
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private ProductJpaEntity toJpa(Product domain) {
        if (domain == null) return null;
        return new ProductJpaEntity(domain.getId(), domain.getName(), domain.getPrice(), domain.getStock());
    }

    private Product toDomain(ProductJpaEntity jpa) {
        if (jpa == null) return null;
        return new Product(jpa.getId(), jpa.getName(), jpa.getPrice(), jpa.getStock());
    }
}
