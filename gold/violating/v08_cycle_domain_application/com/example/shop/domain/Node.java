package com.example.shop.domain;

import com.example.shop.application.Edge;

// VIOLATION (cycle + layered): domain and application reference each other, so
// the packages form a cycle (and the domain must not depend on application).
public class Node {
    public Edge edge;
}
