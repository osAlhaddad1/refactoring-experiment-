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
        CouponEntity entity = EntityMapper.toEntity(coupon);
        CouponEntity saved = repository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return repository.findById(code).map(EntityMapper::toDomain);
    }
}
