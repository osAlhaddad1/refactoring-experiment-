package com.example.shop.infrastructure;

import com.example.shop.domain.Customer;
import com.example.shop.domain.CustomerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, Long> {}

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository springRepository;

    public CustomerRepositoryAdapter(SpringDataCustomerRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity jpaEntity = new CustomerJpaEntity(
                customer.getId(),
                customer.getName(),
                customer.getLoyaltyPoints()
        );
        CustomerJpaEntity saved = springRepository.save(jpaEntity);
        customer.setId(saved.getId());
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return springRepository.findById(id)
                .map(jpa -> new Customer(jpa.getId(), jpa.getName(), jpa.getLoyaltyPoints()));
    }
}