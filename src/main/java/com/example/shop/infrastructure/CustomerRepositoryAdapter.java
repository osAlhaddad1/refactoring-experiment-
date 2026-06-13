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
        entity.id = customer.getId();
        entity.name = customer.getName();
        entity.loyaltyPoints = customer.getLoyaltyPoints();
        
        CustomerEntity saved = repository.save(entity);
        
        customer.setId(saved.id);
        customer.setName(saved.name);
        customer.setLoyaltyPoints(saved.loyaltyPoints);
        return customer;
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
