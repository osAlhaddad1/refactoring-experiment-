package com.example.shop.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderHeaderDto {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public double surcharge;
    public String couponCode;
    public List<OrderLineDto> lines = new ArrayList<>();

    public OrderHeaderDto() {}

    @SuppressWarnings("unchecked")
    public OrderHeaderDto(Map<String, Object> map) {
        if (map != null) {
            this.id = map.get("id") != null ? ((Number) map.get("id")).longValue() : null;
            this.customerId = map.get("customerId") != null ? ((Number) map.get("customerId")).longValue() : null;
            this.status = (String) map.get("status");
            this.total = map.get("total") != null ? ((Number) map.get("total")).doubleValue() : 0.0;
            this.surcharge = map.get("surcharge") != null ? ((Number) map.get("surcharge")).doubleValue() : 0.0;
            this.couponCode = (String) map.get("couponCode");
            if (map.get("lines") != null) {
                List<Map<String, Object>> linesList = (List<Map<String, Object>>) map.get("lines");
                for (Map<String, Object> lineMap : linesList) {
                    this.lines.add(new OrderLineDto(lineMap));
                }
            }
        }
    }
}