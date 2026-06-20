package com.example.shop.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long customerId;
    public String status;
    public double total;
    public double surcharge;

    // sent in by the client, but not stored on the order
    @Transient
    public String couponCode;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    public List<OrderLine> lines = new ArrayList<>();
}