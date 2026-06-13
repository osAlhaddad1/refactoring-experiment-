package com.example.shop.application;

import com.example.shop.domain.StockPort;

// CLEAN: application depends on the domain port only.
public class StockService {

    private final StockPort stockPort;

    public StockService(StockPort stockPort) {
        this.stockPort = stockPort;
    }

    public boolean inStock(long productId) {
        return stockPort.currentStock(productId) > 0;
    }
}
