package com.example.shop.domain;

// CLEAN: domain port; the JPA details live in infrastructure.
public interface CustomerPort {
    String nameOf(long id);
}
