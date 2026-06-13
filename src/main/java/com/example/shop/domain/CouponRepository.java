package com.example.shop.domain;

public interface CouponRepository {
    Coupon save(Coupon coupon);
    Coupon findByCode(String code);
}