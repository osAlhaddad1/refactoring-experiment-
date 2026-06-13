package com.example.shop.domain;

public interface CustomerRepository {
    Customer save(Customer customer);
    Customer findById(Long id);
}