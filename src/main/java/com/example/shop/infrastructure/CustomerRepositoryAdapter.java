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
        CustomerEntity entity = new CustomerEntity();
        entity.setId(customer.getId());
        entity.setName(customer.getName());
        entity.setLoyaltyPoints(customer.getLoyaltyPoints());
        CustomerEntity saved = repository.save(entity);
        return new Customer(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(entity -> new Customer(
            entity.getId(),
            entity.getName(),
            entity.getLoyaltyPoints()
        ));
    }
}