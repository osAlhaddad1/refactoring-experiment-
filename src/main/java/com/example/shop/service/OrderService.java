package com.example.shop.service;

import com.example.shop.model.OrderHeader;
import java.util.Map;

public interface OrderService {
    OrderHeader placeOrder(OrderHeader order);
    OrderHeader getOrder(Long id);
    OrderHeader payOrder(Long id);
    OrderHeader shipOrder(Long id);
    OrderHeader cancelOrder(Long id);
    Map<String, Object> invoiceOrder(Long id);
}