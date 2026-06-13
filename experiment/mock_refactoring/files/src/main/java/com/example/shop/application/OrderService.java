package com.example.shop.application;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderRepository;
import com.example.shop.domain.Pricing;
import com.example.shop.domain.Product;
import com.example.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final ProductRepository products;
    private final OrderRepository orders;
    private final Pricing pricing = new Pricing();

    public OrderService(ProductRepository products, OrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    public OrderView place(Long productId, int quantity) {
        Product product = products.findById(productId)
                .orElseThrow(() -> new ShopException("product not found"));
        if (quantity <= 0) {
            throw new ShopException("quantity must be positive");
        }
        if (!product.hasStock(quantity)) {
            throw new ShopException("not enough stock");
        }

        double total = pricing.totalFor(product.price, quantity);
        product.reduceStock(quantity);
        products.save(product);

        Order order = new Order();
        order.productId = productId;
        order.quantity = quantity;
        order.total = total;
        return toView(orders.save(order));
    }

    public OrderView get(Long id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new ShopException("order not found"));
        return toView(order);
    }

    private OrderView toView(Order order) {
        OrderView view = new OrderView();
        view.id = order.id;
        view.productId = order.productId;
        view.quantity = order.quantity;
        view.total = order.total;
        return view;
    }
}
