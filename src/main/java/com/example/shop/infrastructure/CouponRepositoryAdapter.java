package com.example.shop.infrastructure;

import com.example.shop.domain.Coupon;
import com.example.shop.domain.CouponRepository;
import org.springframework.stereotype.Component;

@Component
public class CouponRepositoryAdapter implements CouponRepository {

    private final SpringCouponRepository springRepository;

    public CouponRepositoryAdapter(SpringCouponRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = new CouponEntity();
        entity.code = coupon.code;
        entity.percent = coupon.percent;
        entity.maxUses = coupon.maxUses;
        entity.timesUsed = coupon.timesUsed;
        CouponEntity saved = springRepository.save(entity);
        return new Coupon(saved.code, saved.percent, saved.maxUses, saved.timesUsed);
    }

    @Override
    public Coupon findByCode(String code) {
        return springRepository.findById(code)
                .map(entity -> new Coupon(entity.code, entity.percent, entity.maxUses, entity.timesUsed))
                .orElse(null);
    }
}