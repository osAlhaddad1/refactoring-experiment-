package com.example.shop.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "coupons")
public class CouponEntity {
    @Id
    public String code;
    public int percent;
    public int maxUses;
    public int timesUsed;
}
