package com.example.shop.infrastructure;

import com.example.shop.domain.Customer;
import com.example.shop.domain.CustomerRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {
    private final SpringDataCustomerRepository repository;

    public CustomerRepositoryAdapter(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity;
        if (customer.getId() != null) {
            entity = repository.findById(customer.getId()).orElseGet(CustomerEntity::new);
        } else {
            entity = new CustomerEntity();
        }
        entity.setName(customer.getName());
        entity.setLoyaltyPoints(customer.getLoyaltyPoints());
        CustomerEntity saved = repository.save(entity);
        customer.setId(saved.getId());
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(entity -> new Customer(entity.getId(), entity.getName(), entity.getLoyaltyPoints()));
    }
}
