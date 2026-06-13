package com.example.shop.infrastructure;

import com.example.shop.domain.Coupon;
import com.example.shop.domain.CouponRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CouponRepositoryAdapter implements CouponRepository {
    private final SpringDataCouponRepository repository;

    public CouponRepositoryAdapter(SpringDataCouponRepository repository) {
        this.repository = repository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity;
        if (coupon.getCode() != null) {
            entity = repository.findById(coupon.getCode()).orElseGet(CouponEntity::new);
        } else {
            entity = new CouponEntity();
        }
        entity.setCode(coupon.getCode());
        entity.setPercent(coupon.getPercent());
        entity.setMaxUses(coupon.getMaxUses());
        entity.setTimesUsed(coupon.getTimesUsed());
        repository.save(entity);
        return coupon;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return repository.findById(code).map(entity -> new Coupon(entity.getCode(), entity.getPercent(), entity.getMaxUses(), entity.getTimesUsed()));
    }
}
