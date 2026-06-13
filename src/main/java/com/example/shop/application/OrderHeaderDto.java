package com.example.shop.application;

import java.util.ArrayList;
import java.util.List;

public class OrderHeaderDto {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public List<OrderLineDto> lines = new ArrayList<>();
}