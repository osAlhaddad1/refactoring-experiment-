package com.example.shop.infrastructure;

import com.example.shop.domain.Coupon;
import com.example.shop.domain.CouponRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CouponRepositoryAdapter implements CouponRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = em.find(CouponEntity.class, coupon.code);
        if (entity == null) {
            entity = new CouponEntity();
            entity.code = coupon.code;
            entity.percent = coupon.percent;
            entity.maxUses = coupon.maxUses;
            entity.timesUsed = coupon.timesUsed;
            em.persist(entity);
        } else {
            entity.percent = coupon.percent;
            entity.maxUses = coupon.maxUses;
            entity.timesUsed = coupon.timesUsed;
            entity = em.merge(entity);
        }
        return coupon;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        CouponEntity entity = em.find(CouponEntity.class, code);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new Coupon(entity.code, entity.percent, entity.maxUses, entity.timesUsed));
    }
}
