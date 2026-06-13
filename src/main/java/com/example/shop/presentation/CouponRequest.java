package com.example.shop.presentation;

public class CouponRequest {
    private String code;
    private int percent;
    private int maxUses;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getPercent() { return percent; }
    public void setPercent(int percent) { this.percent = percent; }
    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }
}
