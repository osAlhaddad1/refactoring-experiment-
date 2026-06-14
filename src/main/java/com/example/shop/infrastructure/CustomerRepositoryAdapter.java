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
        CustomerEntity entity = EntityMapper.toEntity(customer);
        CustomerEntity saved = repository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(EntityMapper::toDomain);
    }
}
