package com.example.shop.domain;

public class Coupon {
    public String code;
    public int percent;
    public int maxUses;
    public int timesUsed;

    public Coupon() {}

    public Coupon(String code, int percent, int maxUses, int timesUsed) {
        this.code = code;
        this.percent = percent;
        this.maxUses = maxUses;
        this.timesUsed = timesUsed;
    }
}
