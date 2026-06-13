package com.example.shop.infrastructure;

import com.example.shop.domain.StockPort;

// CLEAN: infrastructure implements the domain port.
public class StockAdapter implements StockPort {

    public int currentStock(long productId) {
        return 5;
    }
}
