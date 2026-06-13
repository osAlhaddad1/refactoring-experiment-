package com.example.shop.domain;

import java.util.List;

public class OrderPricingEngine {

    public void calculatePriceAndReduceStock(OrderHeader order, List<Product> products, Coupon coupon) {
        if (order.lines == null || order.lines.isEmpty()) {
            throw new DomainException("order has no lines");
        }

        double subtotal = 0;
        for (OrderLine line : order.lines) {
            Product product = products.stream()
                    .filter(p -> p.id.equals(line.productId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("product"));

            if (line.quantity <= 0) {
                throw new DomainException("quantity must be positive");
            }
            if (product.stock < line.quantity) {
                throw new DomainException("not enough stock");
            }

            double linePrice = product.price * line.quantity;
            if (line.quantity >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            line.linePrice = linePrice;
            subtotal += linePrice;
            product.stock = product.stock - line.quantity;
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        if (order.couponCode != null && !order.couponCode.isEmpty()) {
            if (coupon == null) {
                throw new DomainException("unknown coupon");
            }
            if (coupon.timesUsed >= coupon.maxUses) {
                throw new DomainException("coupon has been used up");
            }
            total = total * (100 - coupon.percent) / 100;
            coupon.timesUsed = coupon.timesUsed + 1;
        }

        order.total = total;
    }

    public void pay(OrderHeader order, Customer customer) {
        if (!"NEW".equals(order.status)) {
            throw new DomainException("only NEW orders can be paid");
        }
        order.status = "PAID";
        order.surcharge = 5.0;
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
    }

    public void ship(OrderHeader order) {
        if (!"PAID".equals(order.status)) {
            throw new DomainException("only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
    }

    public void cancel(OrderHeader order, List<Product> products) {
        if ("SHIPPED".equals(order.status) || "CANCELLED".equals(order.status)) {
            throw new DomainException("cannot cancel a " + order.status + " order");
        }
        for (OrderLine line : order.lines) {
            products.stream()
                    .filter(p -> p.id.equals(line.productId))
                    .findFirst()
                    .ifPresent(product -> product.stock = product.stock + line.quantity);
        }
        order.status = "CANCELLED";
    }
}
