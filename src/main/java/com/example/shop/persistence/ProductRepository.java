package com.example.shop.persistence;

import com.example.shop.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductRepository {
    @PersistenceContext
    private EntityManager em;

    public Product save(Product product) {
        if (product.id == null) {
            em.persist(product);
            return product;
        } else {
            return em.merge(product);
        }
    }

    public Product findById(Long id) {
        return em.find(Product.class, id);
    }

    public List<Product> findAll() {
        return em.createQuery("select p from Product p", Product.class).getResultList();
    }
}