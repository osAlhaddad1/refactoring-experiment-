package com.example.shop.domain;

import java.util.ArrayList;
import java.util.List;

public class OrderHeader {
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public List<OrderLine> lines = new ArrayList<>();
}