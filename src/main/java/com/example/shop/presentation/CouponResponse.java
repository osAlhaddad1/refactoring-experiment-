package com.example.shop.presentation;

public class CouponResponse {
    private String code;
    private int percent;
    private int maxUses;
    private int timesUsed;

    public CouponResponse() {}
    public CouponResponse(String code, int percent, int maxUses, int timesUsed) {
        this.code = code;
        this.percent = percent;
        this.maxUses = maxUses;
        this.timesUsed = timesUsed;
    }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getPercent() { return percent; }
    public void setPercent(int percent) { this.percent = percent; }
    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }
    public int getTimesUsed() { return timesUsed; }
    public void setTimesUsed(int timesUsed) { this.timesUsed = timesUsed; }
}
