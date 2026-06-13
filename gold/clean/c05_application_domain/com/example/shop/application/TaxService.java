package com.example.shop.application;

import com.example.shop.domain.TaxPort;

// CLEAN: application -> domain (through the port).
public class TaxService {

    private final TaxPort taxPort;

    public TaxService(TaxPort taxPort) {
        this.taxPort = taxPort;
    }

    public double withTax(double amount) {
        return amount * (1 + taxPort.rate());
    }
}
