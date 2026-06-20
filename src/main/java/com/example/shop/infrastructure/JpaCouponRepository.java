package com.example.shop.infrastructure;

import com.example.shop.domain.Coupon;
import com.example.shop.domain.CouponRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class JpaCouponRepository implements CouponRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = new CouponEntity();
        entity.code = coupon.getCode();
        entity.percent = coupon.getPercent();
        entity.maxUses = coupon.getMaxUses();
        entity.timesUsed = coupon.getTimesUsed();
        if (em.find(CouponEntity.class, coupon.getCode()) == null) {
            em.persist(entity);
        } else {
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