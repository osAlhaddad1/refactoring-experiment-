package com.example.shop.application;

import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public ProductView create(String name, double price, int stock) {
        Product product = new Product();
        product.name = name;
        product.price = price;
        product.stock = stock;
        return toView(products.save(product));
    }

    public ProductView get(Long id) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ShopException("product not found"));
        return toView(product);
    }

    public List<ProductView> list() {
        List<ProductView> views = new ArrayList<>();
        for (Product product : products.findAll()) {
            views.add(toView(product));
        }
        return views;
    }

    private ProductView toView(Product product) {
        ProductView view = new ProductView();
        view.id = product.id;
        view.name = product.name;
        view.price = product.price;
        view.stock = product.stock;
        return view;
    }
}
