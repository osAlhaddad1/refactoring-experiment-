package com.example.shop.application;

public class InvoiceDto {
    public long invoiceNumber;
    public Long orderId;
    public double total;
    public double surcharge;
    public double amountDue;

    public InvoiceDto() {}
    public InvoiceDto(long invoiceNumber, Long orderId, double total, double surcharge, double amountDue) {
        this.invoiceNumber = invoiceNumber;
        this.orderId = orderId;
        this.total = total;
        this.surcharge = surcharge;
        this.amountDue = amountDue;
    }
}
