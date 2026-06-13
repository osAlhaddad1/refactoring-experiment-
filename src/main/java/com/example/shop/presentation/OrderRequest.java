package com.example.shop.presentation;

import java.util.List;

public class OrderRequest {
    public Long customerId;
    public String couponCode;
    public List<OrderLineRequest> lines;
}
