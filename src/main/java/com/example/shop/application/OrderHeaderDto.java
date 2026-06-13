package com.example.shop.application;

import java.util.ArrayList;
import java.util.List;

public class OrderHeaderDto {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public double surcharge;
    public String couponCode;
    public List<OrderLineDto> lines = new ArrayList<>();
}
