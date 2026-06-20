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
        if (customer.getId() == null) {
            entity = new CustomerEntity();
        } else {
            entity = em.find(CustomerEntity.class, customer.getId());
            if (entity == null) {
                entity = new CustomerEntity();
            }
        }
        entity.name = customer.getName();
        entity.loyaltyPoints = customer.getLoyaltyPoints();

        if (entity.id == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        return toDomain(entity);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        CustomerEntity entity = em.find(CustomerEntity.class, id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    private Customer toDomain(CustomerEntity entity) {
        Customer customer = new Customer();
        customer.setId(entity.id);
        customer.setName(entity.name);
        customer.setLoyaltyPoints(entity.loyaltyPoints);
        return customer;
    }
}
