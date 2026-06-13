package com.example.shop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    public String code;   // the coupon code is its natural id
    public int percent;
    public int maxUses;
    public int timesUsed;
}