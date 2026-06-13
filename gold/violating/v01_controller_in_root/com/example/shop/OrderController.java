package com.example.shop;

// VIOLATION (naming): a *Controller must live in ..presentation.., not in the
// root package.
public class OrderController {

    public String hello() {
        return "hi";
    }
}
