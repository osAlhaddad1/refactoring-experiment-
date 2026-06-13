package com.example.shop.domain;

// CLEAN: pure domain business logic.
public class Discount {

    public double apply(double total, int quantity) {
        if (quantity >= 10) {
            return total * 0.9;
        }
        return total;
    }
}
