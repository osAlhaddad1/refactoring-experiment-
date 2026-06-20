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
        CustomerEntity entity;
        if (customer.id == null) {
            entity = new CustomerEntity();
            entity.name = customer.name;
            entity.loyaltyPoints = customer.loyaltyPoints;
            em.persist(entity);
        } else {
            entity = em.find(CustomerEntity.class, customer.id);
            if (entity != null) {
                entity.name = customer.name;
                entity.loyaltyPoints = customer.loyaltyPoints;
                entity = em.merge(entity);
            }
        }
        customer.id = entity.id;
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        CustomerEntity entity = em.find(CustomerEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new Customer(entity.id, entity.name, entity.loyaltyPoints));
    }
}
