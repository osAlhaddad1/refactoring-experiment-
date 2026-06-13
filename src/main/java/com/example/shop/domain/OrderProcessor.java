package com.example.shop.domain;

import java.util.List;

public class OrderProcessor {

    public static void processOrderPlacement(
            OrderHeader order,
            Customer customer,
            List<Product> products,
            Coupon coupon) {

        if (order.getLines() == null || order.getLines().isEmpty()) {
            throw new DomainException("order has no lines", false);
        }

        double subtotal = 0;
        for (OrderLine line : order.getLines()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(line.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new DomainException("product not found", true));

            if (line.getQuantity() <= 0) {
                throw new DomainException("quantity must be positive", false);
            }
            if (product.getStock() < line.getQuantity()) {
                throw new DomainException("not enough stock", false);
            }

            double linePrice = product.getPrice() * line.getQuantity();
            if (line.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            line.setLinePrice(linePrice);
            subtotal += linePrice;
            product.setStock(product.getStock() - line.getQuantity());
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        if (order.getCouponCode() != null && !order.getCouponCode().isEmpty()) {
            if (coupon == null) {
                throw new DomainException("unknown coupon", false);
            }
            if (coupon.getTimesUsed() >= coupon.getMaxUses()) {
                throw new DomainException("coupon has been used up", false);
            }
            total = total * (100 - coupon.getPercent()) / 100;
            coupon.setTimesUsed(coupon.getTimesUsed() + 1);
        }

        order.setId(null);
        order.setStatus("NEW");
        order.setTotal(total);
        order.setSurcharge(0);
    }

    public static void payOrder(OrderHeader order, Customer customer) {
        if (!"NEW".equals(order.getStatus())) {
            throw new DomainException("only NEW orders can be paid", false);
        }
        order.setStatus("PAID");
        order.setSurcharge(5.0);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
    }

    public static void shipOrder(OrderHeader order) {
        if (!"PAID".equals(order.getStatus())) {
            throw new DomainException("only PAID orders can be shipped", false);
        }
        order.setStatus("SHIPPED");
    }

    public static void cancelOrder(OrderHeader order, List<Product> products) {
        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new DomainException("cannot cancel a " + order.getStatus() + " order", false);
        }
        for (OrderLine line : order.getLines()) {
            products.stream()
                    .filter(p -> p.getId().equals(line.getProductId()))
                    .findFirst()
                    .ifPresent(product -> product.setStock(product.getStock() + line.getQuantity()));
        }
        order.setStatus("CANCELLED");
    }
}
