package com.example.shop.domain;

// Pure business rule: bulk discount of 10% for 10 items or more.
public class Pricing {

    public double totalFor(double price, int quantity) {
        double total = price * quantity;
        if (quantity >= 10) {
            total = total * 0.9;
        }
        return total;
    }
}
