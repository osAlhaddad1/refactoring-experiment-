package com.example.shop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    public String code;
    public int percent;
    public int maxUses;
    public int timesUsed;
}