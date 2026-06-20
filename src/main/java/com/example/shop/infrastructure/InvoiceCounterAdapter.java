package com.example.shop.infrastructure;

import com.example.shop.domain.InvoiceCounterPort;
import org.springframework.stereotype.Component;

@Component
public class InvoiceCounterAdapter implements InvoiceCounterPort {
    private static long INVOICE_COUNTER = 0;

    @Override
    public synchronized long incrementAndGet() {
        return ++INVOICE_COUNTER;
    }
}