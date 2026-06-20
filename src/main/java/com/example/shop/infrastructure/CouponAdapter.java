package com.example.shop.infrastructure;

import com.example.shop.domain.Coupon;
import com.example.shop.domain.CouponRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CouponAdapter implements CouponRepository {
    private final SpringDataCouponRepository repository;

    public CouponAdapter(SpringDataCouponRepository repository) {
        this.repository = repository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = new CouponEntity();
        entity.code = coupon.code;
        entity.percent = coupon.percent;
        entity.maxUses = coupon.maxUses;
        entity.timesUsed = coupon.timesUsed;
        entity = repository.save(entity);
        coupon.code = entity.code;
        return coupon;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return repository.findById(code).map(entity -> {
            Coupon coupon = new Coupon();
            coupon.code = entity.code;
            coupon.percent = entity.percent;
            coupon.maxUses = entity.maxUses;
            coupon.timesUsed = entity.timesUsed;
            return coupon;
        });
    }
}
