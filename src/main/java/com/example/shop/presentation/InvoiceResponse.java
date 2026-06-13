package com.example.shop.presentation;

public class InvoiceResponse {
    public long invoiceNumber;
    public Long orderId;
    public double total;
    public double surcharge;
    public double amountDue;

    public InvoiceResponse() {}
    public InvoiceResponse(long invoiceNumber, Long orderId, double total, double surcharge, double amountDue) {
        this.invoiceNumber = invoiceNumber;
        this.orderId = orderId;
        this.total = total;
        this.surcharge = surcharge;
        this.amountDue = amountDue;
    }
}
