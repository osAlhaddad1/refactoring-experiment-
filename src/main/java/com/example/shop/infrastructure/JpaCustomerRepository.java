package com.example.shop.infrastructure;

import com.example.shop.domain.Customer;
import com.example.shop.domain.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class JpaCustomerRepository implements CustomerRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.id = customer.getId();
        entity.name = customer.getName();
        entity.loyaltyPoints = customer.getLoyaltyPoints();
        if (entity.id == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        customer.setId(entity.id);
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