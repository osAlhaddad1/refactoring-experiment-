package com.example.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringCouponRepository extends JpaRepository<CouponEntity, String> {
}