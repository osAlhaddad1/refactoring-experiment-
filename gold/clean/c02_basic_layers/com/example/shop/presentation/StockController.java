package com.example.shop.presentation;

import com.example.shop.application.StockService;

// CLEAN: presentation depends on application only.
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    public boolean check(long productId) {
        return stockService.inStock(productId);
    }
}
