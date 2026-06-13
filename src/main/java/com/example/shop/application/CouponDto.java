package com.example.shop.application;

public class CouponDto {
    public String code;
    public int percent;
    public int maxUses;
    public int timesUsed;

    public CouponDto() {}
    public CouponDto(String code, int percent, int maxUses, int timesUsed) {
        this.code = code;
        this.percent = percent;
        this.maxUses = maxUses;
        this.timesUsed = timesUsed;
    }
}
