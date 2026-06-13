package com.example.shop.presentation;

import java.util.Map;

public class CustomerDto {
    public Long id;
    public String name;
    public int loyaltyPoints;

    public CustomerDto() {}

    public CustomerDto(Map<String, Object> map) {
        if (map != null) {
            this.id = map.get("id") != null ? ((Number) map.get("id")).longValue() : null;
            this.name = (String) map.get("name");
            this.loyaltyPoints = map.get("loyaltyPoints") != null ? ((Number) map.get("loyaltyPoints")).intValue() : 0;
        }
    }
}