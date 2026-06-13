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
    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = new Customer();
        customer.setId(null);
        customer.setName(dto.getName());
        customer.setLoyaltyPoints(0);
        Customer saved = customerRepository.save(customer);
        return new CustomerDto(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("customer not found"));
        return new CustomerDto(customer.getId(), customer.getName(), customer.getLoyaltyPoints());
    }
}
