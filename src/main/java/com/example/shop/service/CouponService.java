package com.example.shop.service;

import com.example.shop.model.Coupon;
import com.example.shop.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponService {
    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        coupon.timesUsed = 0;
        return couponRepository.save(coupon);
    }
}