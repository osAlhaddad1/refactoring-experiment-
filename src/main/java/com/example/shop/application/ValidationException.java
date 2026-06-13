package com.example.shop.application;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
