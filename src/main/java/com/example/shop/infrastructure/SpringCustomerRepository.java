package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringCustomerRepository extends JpaRepository<CustomerEntity, Long> {
}