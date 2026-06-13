package com.example.shop.domain;

import org.springframework.stereotype.Component;

// VIOLATION (domain purity): the domain must not import org.springframework.
@Component
public class PriceCalculator {

    public double total(double price, int quantity) {
        return price * quantity;
    }
}
