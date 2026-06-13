package com.example.shop.presentation;

import java.util.Map;

public class OrderLineDto {
    public Long id;
    public Long productId;
    public int quantity;
    public double linePrice;

    public OrderLineDto() {}

    public OrderLineDto(Map<String, Object> map) {
        if (map != null) {
            this.id = map.get("id") != null ? ((Number) map.get("id")).longValue() : null;
            this.productId = map.get("productId") != null ? ((Number) map.get("productId")).longValue() : null;
            this.quantity = map.get("quantity") != null ? ((Number) map.get("quantity")).intValue() : 0;
            this.linePrice = map.get("linePrice") != null ? ((Number) map.get("linePrice")).doubleValue() : 0.0;
        }
    }
}