package com.example.shop.infrastructure;

import com.example.shop.domain.Customer;
import com.example.shop.domain.CustomerRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {
    private final CustomerJpaRepository repository;

    public CustomerRepositoryAdapter(CustomerJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.id = customer.id;
        entity.name = customer.name;
        entity.loyaltyPoints = customer.loyaltyPoints;
        
        CustomerEntity saved = repository.save(entity);
        
        customer.id = saved.id;
        customer.name = saved.name;
        customer.loyaltyPoints = saved.loyaltyPoints;
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(entity -> {
            Customer customer = new Customer();
            customer.id = entity.id;
            customer.name = entity.name;
            customer.loyaltyPoints = entity.loyaltyPoints;
            return customer;
        });
    }
}