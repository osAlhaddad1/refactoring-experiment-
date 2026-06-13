package com.example.shop.infrastructure;

import com.example.shop.domain.Coupon;
import com.example.shop.domain.CouponRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CouponRepositoryAdapter implements CouponRepository {

    private final JpaCouponRepository jpaRepository;

    public CouponRepositoryAdapter(JpaCouponRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = toEntity(coupon);
        CouponEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return jpaRepository.findById(code).map(this::toDomain);
    }

    private CouponEntity toEntity(Coupon domain) {
        if (domain == null) return null;
        CouponEntity entity = new CouponEntity();
        entity.setCode(domain.getCode());
        entity.setPercent(domain.getPercent());
        entity.setMaxUses(domain.getMaxUses());
        entity.setTimesUsed(domain.getTimesUsed());
        return entity;
    }

    private Coupon toDomain(CouponEntity entity) {
        if (entity == null) return null;
        return new Coupon(entity.getCode(), entity.getPercent(), entity.getMaxUses(), entity.getTimesUsed());
    }
}