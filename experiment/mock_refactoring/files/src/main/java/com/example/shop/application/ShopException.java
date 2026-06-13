package com.example.shop.application;

// Application error; the presentation layer maps it to HTTP 400.
public class ShopException extends RuntimeException {
    public ShopException(String message) {
        super(message);
    }
}
