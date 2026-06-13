package com.example.shop.presentation;

import com.example.shop.domain.Cart;

// VIOLATION (layered): presentation may only depend on application, not reach
// straight into the domain.
public class CartController {

    private final Cart cart = new Cart();

    public int size() {
        return cart.size;
    }
}
