package com.example.shop.application;

import com.example.shop.domain.Customer;
import com.example.shop.domain.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerInfo createCustomer(String name) {
        Customer customer = new Customer(null, name, 0);
        Customer saved = customerRepository.save(customer);
        return new CustomerInfo(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    public CustomerInfo getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("customer"));
        return new CustomerInfo(customer.getId(), customer.getName(), customer.getLoyaltyPoints());
    }
}