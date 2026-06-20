package com.example.shop.infrastructure;

import com.example.shop.domain.InvoicePort;
import org.springframework.stereotype.Component;

@Component
public class InMemoryInvoiceAdapter implements InvoicePort {
    private static long INVOICE_COUNTER = 0;

    @Override
    public synchronized long nextInvoiceNumber() {
        return ++INVOICE_COUNTER;
    }
}