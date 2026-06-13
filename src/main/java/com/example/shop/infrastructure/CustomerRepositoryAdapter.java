package com.example.shop.infrastructure;

import com.example.shop.domain.Customer;
import com.example.shop.domain.CustomerRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final SpringCustomerRepository springRepository;

    public CustomerRepositoryAdapter(SpringCustomerRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.id = customer.id;
        entity.name = customer.name;
        entity.loyaltyPoints = customer.loyaltyPoints;
        CustomerEntity saved = springRepository.save(entity);
        return new Customer(saved.id, saved.name, saved.loyaltyPoints);
    }

    @Override
    public Customer findById(Long id) {
        return springRepository.findById(id)
                .map(entity -> new Customer(entity.id, entity.name, entity.loyaltyPoints))
                .orElse(null);
    }
}