package com.example.shop.application;

import com.example.shop.domain.*;
import java.util.stream.Collectors;

public class DtoMapper {
    public static CategoryDto toDto(Category category) {
        if (category == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.id = category.id;
        dto.name = category.name;
        return dto;
    }

    public static Category toDomain(CategoryDto dto) {
        if (dto == null) return null;
        Category category = new Category();
        category.id = dto.id;
        category.name = dto.name;
        return category;
    }

    public static CustomerDto toDto(Customer customer) {
        if (customer == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.id = customer.id;
        dto.name = customer.name;
        dto.loyaltyPoints = customer.loyaltyPoints;
        return dto;
    }

    public static Customer toDomain(CustomerDto dto) {
        if (dto == null) return null;
        Customer customer = new Customer();
        customer.id = dto.id;
        customer.name = dto.name;
        customer.loyaltyPoints = dto.loyaltyPoints;
        return customer;
    }

    public static CouponDto toDto(Coupon coupon) {
        if (coupon == null) return null;
        CouponDto dto = new CouponDto();
        dto.code = coupon.code;
        dto.percent = coupon.percent;
        dto.maxUses = coupon.maxUses;
        dto.timesUsed = coupon.timesUsed;
        return dto;
    }

    public static Coupon toDomain(CouponDto dto) {
        if (dto == null) return null;
        Coupon coupon = new Coupon();
        coupon.code = dto.code;
        coupon.percent = dto.percent;
        coupon.maxUses = dto.maxUses;
        coupon.timesUsed = dto.timesUsed;
        return coupon;
    }

    public static ProductDto toDto(Product product) {
        if (product == null) return null;
        ProductDto dto = new ProductDto();
        dto.id = product.id;
        dto.name = product.name;
        dto.price = product.price;
        dto.stock = product.stock;
        dto.category = toDto(product.category);
        return dto;
    }

    public static OrderDto toDto(OrderHeader order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        dto.id = order.id;
        dto.customerId = order.customerId;
        dto.status = order.status;
        dto.total = order.total;
        dto.surcharge = order.surcharge;
        dto.couponCode = order.couponCode;
        if (order.lines != null) {
            dto.lines = order.lines.stream().map(DtoMapper::toDto).collect(Collectors.toList());
        }
        return dto;
    }

    public static OrderLineDto toDto(OrderLine line) {
        if (line == null) return null;
        OrderLineDto dto = new OrderLineDto();
        dto.id = line.id;
        dto.productId = line.productId;
        dto.quantity = line.quantity;
        dto.linePrice = line.linePrice;
        return dto;
    }

    public static OrderHeader toDomain(OrderDto dto) {
        if (dto == null) return null;
        OrderHeader order = new OrderHeader();
        order.id = dto.id;
        order.customerId = dto.customerId;
        order.status = dto.status;
        order.total = dto.total;
        order.surcharge = dto.surcharge;
        order.couponCode = dto.couponCode;
        if (dto.lines != null) {
            order.lines = dto.lines.stream().map(DtoMapper::toDomain).collect(Collectors.toList());
        }
        return order;
    }

    public static OrderLine toDomain(OrderLineDto dto) {
        if (dto == null) return null;
        OrderLine line = new OrderLine();
        line.id = dto.id;
        line.productId = dto.productId;
        line.quantity = dto.quantity;
        line.linePrice = dto.linePrice;
        return line;
    }
}
