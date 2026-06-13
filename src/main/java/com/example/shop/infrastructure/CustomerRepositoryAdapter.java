package com.example.shop.infrastructure;

import com.example.shop.domain.Customer;
import com.example.shop.domain.CustomerRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {
    private final CustomerJpaRepository repository;

    public CustomerRepositoryAdapter(CustomerJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.id = customer.getId();
        entity.name = customer.getName();
        entity.loyaltyPoints = customer.getLoyaltyPoints();
        CustomerJpaEntity saved = repository.save(entity);
        
        Customer savedCustomer = new Customer();
        savedCustomer.setId(saved.id);
        savedCustomer.setName(saved.name);
        savedCustomer.setLoyaltyPoints(saved.loyaltyPoints);
        return savedCustomer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(entity -> {
            Customer customer = new Customer();
            customer.setId(entity.id);
            customer.setName(entity.name);
            customer.setLoyaltyPoints(entity.loyaltyPoints);
            return customer;
        });
    }
}