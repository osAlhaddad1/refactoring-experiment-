package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSpringRepository extends JpaRepository<CustomerJpaEntity, Long> {
}
