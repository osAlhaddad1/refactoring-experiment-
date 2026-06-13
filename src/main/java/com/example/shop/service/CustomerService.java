package com.example.shop.service;

import com.example.shop.model.Customer;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    Customer getCustomer(Long id);
}