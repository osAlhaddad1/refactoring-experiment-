package com.example.shop.presentation;

import java.util.Map;

public class CouponDto {
    public String code;
    public int percent;
    public int maxUses;
    public int timesUsed;

    public CouponDto() {}

    public CouponDto(Map<String, Object> map) {
        if (map != null) {
            this.code = (String) map.get("code");
            this.percent = map.get("percent") != null ? ((Number) map.get("percent")).intValue() : 0;
            this.maxUses = map.get("maxUses") != null ? ((Number) map.get("maxUses")).intValue() : 0;
            this.timesUsed = map.get("timesUsed") != null ? ((Number) map.get("timesUsed")).intValue() : 0;
        }
    }
}