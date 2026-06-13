package com.example.shop.domain;

public class DomainException extends RuntimeException {
    private final boolean notFound;

    public DomainException(String message, boolean notFound) {
        super(message);
        this.notFound = notFound;
    }

    public boolean isNotFound() {
        return notFound;
    }
}
