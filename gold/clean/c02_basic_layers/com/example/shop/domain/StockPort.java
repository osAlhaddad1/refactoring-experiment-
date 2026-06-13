package com.example.shop.domain;

// CLEAN: the domain owns the port interface.
public interface StockPort {
    int currentStock(long productId);
}
