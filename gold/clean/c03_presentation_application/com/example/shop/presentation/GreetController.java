package com.example.shop.presentation;

import com.example.shop.application.GreetService;

// CLEAN: presentation -> application.
public class GreetController {

    private final GreetService greetService;

    public GreetController(GreetService greetService) {
        this.greetService = greetService;
    }

    public String greet(String name) {
        return greetService.greet(name);
    }
}
