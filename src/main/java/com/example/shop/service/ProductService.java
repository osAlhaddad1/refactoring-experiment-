package com.example.shop.service;

import com.example.shop.model.Product;
import java.util.Map;

public interface ProductService {
    Product createProduct(Map<String, Object> body);
    Product getProduct(Long id);
}