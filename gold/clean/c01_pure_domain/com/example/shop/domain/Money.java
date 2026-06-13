package com.example.shop.domain;

// CLEAN: a pure domain value object, no frameworks, no other layers.
public class Money {

    public final long cents;

    public Money(long cents) {
        this.cents = cents;
    }

    public Money plus(Money other) {
        return new Money(this.cents + other.cents);
    }
}
