package com.example.shop.application;

// Application-level result carrier (so presentation never touches the domain).
public class ProductView {
    public Long id;
    public String name;
    public double price;
    public int stock;
}
