package com.example.shop.presentation;

public class CouponResponse {
    public String code;
    public int percent;
    public int maxUses;
    public int timesUsed;

    public CouponResponse() {}
    public CouponResponse(String code, int percent, int maxUses, int timesUsed) {
        this.code = code;
        this.percent = percent;
        this.maxUses = maxUses;
        this.timesUsed = timesUsed;
    }
}
