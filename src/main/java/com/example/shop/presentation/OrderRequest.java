package com.example.shop.presentation;

import java.util.List;

public class OrderRequest {
    public Long customerId;
    public List<OrderLineRequest> lines;
}