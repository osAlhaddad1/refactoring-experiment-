package com.example.shop.presentation;

import java.util.List;

public class OrderResponse {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public List<OrderLineResponse> lines;
}
