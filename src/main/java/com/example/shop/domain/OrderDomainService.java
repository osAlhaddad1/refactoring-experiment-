package com.example.shop.domain;

import com.example.shop.domain.exception.BadRequestException;
import com.example.shop.domain.exception.NotFoundException;
import java.util.List;

public class OrderDomainService {

    public void placeOrder(OrderHeader order, Customer customer, List<Product> products, Coupon coupon) {
        if (order.getLines() == null || order.getLines().isEmpty()) {
            throw new BadRequestException("order has no lines");
        }

        double subtotal = 0;
        for (OrderLine line : order.getLines()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(line.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("product not found"));

            if (line.getQuantity() <= 0) {
                throw new BadRequestException("quantity must be positive");
            }
            if (product.getStock() < line.getQuantity()) {
                throw new BadRequestException("not enough stock");
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
                throw new BadRequestException("unknown coupon");
            }
            if (coupon.getTimesUsed() >= coupon.getMaxUses()) {
                throw new BadRequestException("coupon has been used up");
            }
            total = total * (100 - coupon.getPercent()) / 100;
            coupon.setTimesUsed(coupon.getTimesUsed() + 1);
        }

        order.setId(null);
        order.setStatus("NEW");
        order.setTotal(total);
        order.setSurcharge(0);
    }

    public void payOrder(OrderHeader order, Customer customer) {
        if (!"NEW".equals(order.getStatus())) {
            throw new BadRequestException("only NEW orders can be paid");
        }
        order.setStatus("PAID");
        order.setSurcharge(5.0);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
    }

    public void shipOrder(OrderHeader order) {
        if (!"PAID".equals(order.getStatus())) {
            throw new BadRequestException("only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");
    }

    public void cancelOrder(OrderHeader order, List<Product> products) {
        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BadRequestException("cannot cancel a " + order.getStatus() + " order");
        }
        for (OrderLine line : order.getLines()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(line.getProductId()))
                    .findFirst()
                    .orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + line.getQuantity());
            }
        }
        order.setStatus("CANCELLED");
    }
}