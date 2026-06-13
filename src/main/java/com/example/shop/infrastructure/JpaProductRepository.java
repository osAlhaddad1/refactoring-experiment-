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
public class JpaProductRepository implements ProductRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            ProductJpaEntity jpaEntity = new ProductJpaEntity(
                    null,
                    product.getName(),
                    product.getPrice(),
                    product.getStock()
            );
            em.persist(jpaEntity);
            product.setId(jpaEntity.getId());
        } else {
            ProductJpaEntity jpaEntity = em.find(ProductJpaEntity.class, product.getId());
            if (jpaEntity != null) {
                jpaEntity.setName(product.getName());
                jpaEntity.setPrice(product.getPrice());
                jpaEntity.setStock(product.getStock());
                em.merge(jpaEntity);
            }
        }
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        ProductJpaEntity jpaEntity = em.find(ProductJpaEntity.class, id);
        if (jpaEntity == null) {
            return Optional.empty();
        }
        return Optional.of(new Product(
                jpaEntity.getId(),
                jpaEntity.getName(),
                jpaEntity.getPrice(),
                jpaEntity.getStock()
        ));
    }

    @Override
    public List<Product> findAll() {
        List<ProductJpaEntity> entities = em.createQuery("select p from ProductJpaEntity p", ProductJpaEntity.class)
                .getResultList();
        return entities.stream()
                .map(jpaEntity -> new Product(
                        jpaEntity.getId(),
                        jpaEntity.getName(),
                        jpaEntity.getPrice(),
                        jpaEntity.getStock()
                ))
                .collect(Collectors.toList());
    }
}
