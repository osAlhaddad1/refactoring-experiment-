package com.example.shop.infrastructure;

import com.example.shop.domain.Customer;
import com.example.shop.domain.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity(customer.getId(), customer.getName(), customer.getLoyaltyPoints());
        if (entity.getId() == null) {
            em.persist(entity);
            em.flush();
            customer.setId(entity.getId());
        } else {
            entity = em.merge(entity);
            em.flush();
        }
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        CustomerJpaEntity entity = em.find(CustomerJpaEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new Customer(entity.getId(), entity.getName(), entity.getLoyaltyPoints()));
    }
}