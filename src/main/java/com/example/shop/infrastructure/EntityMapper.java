package com.example.shop.infrastructure;

import com.example.shop.domain.*;
import java.util.stream.Collectors;

public class EntityMapper {
    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;
        Category category = new Category();
        category.id = entity.id;
        category.name = entity.name;
        return category;
    }

    public static CategoryEntity toEntity(Category category) {
        if (category == null) return null;
        CategoryEntity entity = new CategoryEntity();
        entity.id = category.id;
        entity.name = category.name;
        return entity;
    }

    public static Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;
        Customer customer = new Customer();
        customer.id = entity.id;
        customer.name = entity.name;
        customer.loyaltyPoints = entity.loyaltyPoints;
        return customer;
    }

    public static CustomerEntity toEntity(Customer customer) {
        if (customer == null) return null;
        CustomerEntity entity = new CustomerEntity();
        entity.id = customer.id;
        entity.name = customer.name;
        entity.loyaltyPoints = customer.loyaltyPoints;
        return entity;
    }

    public static Coupon toDomain(CouponEntity entity) {
        if (entity == null) return null;
        Coupon coupon = new Coupon();
        coupon.code = entity.code;
        coupon.percent = entity.percent;
        coupon.maxUses = entity.maxUses;
        coupon.timesUsed = entity.timesUsed;
        return coupon;
    }

    public static CouponEntity toEntity(Coupon coupon) {
        if (coupon == null) return null;
        CouponEntity entity = new CouponEntity();
        entity.code = coupon.code;
        entity.percent = coupon.percent;
        entity.maxUses = coupon.maxUses;
        entity.timesUsed = coupon.timesUsed;
        return entity;
    }

    public static Product toDomain(ProductEntity entity) {
        if (entity == null) return null;
        Product product = new Product();
        product.id = entity.id;
        product.name = entity.name;
        product.price = entity.price;
        product.stock = entity.stock;
        product.category = toDomain(entity.category);
        return product;
    }

    public static ProductEntity toEntity(Product product) {
        if (product == null) return null;
        ProductEntity entity = new ProductEntity();
        entity.id = product.id;
        entity.name = product.name;
        entity.price = product.price;
        entity.stock = product.stock;
        entity.category = toEntity(product.category);
        return entity;
    }

    public static OrderHeader toDomain(OrderHeaderEntity entity) {
        if (entity == null) return null;
        OrderHeader order = new OrderHeader();
        order.id = entity.id;
        order.customerId = entity.customerId;
        order.status = entity.status;
        order.total = entity.total;
        order.surcharge = entity.surcharge;
        if (entity.lines != null) {
            order.lines = entity.lines.stream().map(EntityMapper::toDomain).collect(Collectors.toList());
        }
        return order;
    }

    public static OrderHeaderEntity toEntity(OrderHeader order) {
        if (order == null) return null;
        OrderHeaderEntity entity = new OrderHeaderEntity();
        entity.id = order.id;
        entity.customerId = order.customerId;
        entity.status = order.status;
        entity.total = order.total;
        entity.surcharge = order.surcharge;
        if (order.lines != null) {
            entity.lines = order.lines.stream().map(EntityMapper::toEntity).collect(Collectors.toList());
        }
        return entity;
    }

    public static OrderLine toDomain(OrderLineEntity entity) {
        if (entity == null) return null;
        OrderLine line = new OrderLine();
        line.id = entity.id;
        line.productId = entity.productId;
        line.quantity = entity.quantity;
        line.linePrice = entity.linePrice;
        return line;
    }

    public static OrderLineEntity toEntity(OrderLine line) {
        if (line == null) return null;
        OrderLineEntity entity = new OrderLineEntity();
        entity.id = line.id;
        entity.productId = line.productId;
        entity.quantity = line.quantity;
        entity.linePrice = line.linePrice;
        return entity;
    }
}
