package com.example.shop.presentation;

import java.util.List;

public class PlaceOrderRequest {
    public Long customerId;
    public List<PlaceOrderLineRequest> lines;
}
