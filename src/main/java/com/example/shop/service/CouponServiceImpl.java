package com.example.shop.service;

import com.example.shop.model.Coupon;
import com.example.shop.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        coupon.timesUsed = 0;
        return couponRepository.save(coupon);
    }
}