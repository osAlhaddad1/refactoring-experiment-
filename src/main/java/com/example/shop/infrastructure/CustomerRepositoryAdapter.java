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
        CustomerJpaEntity entity = toEntity(customer);
        CustomerJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    private CustomerJpaEntity toEntity(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(customer.getId());
        entity.setName(customer.getName());
        entity.setLoyaltyPoints(customer.getLoyaltyPoints());
        return entity;
    }

    private Customer toDomain(CustomerJpaEntity entity) {
        return new Customer(entity.getId(), entity.getName(), entity.getLoyaltyPoints());
    }
}
