package com.cloud.aws.model;

public class InvoiceResponse {
    private Invoice invoice;
    private String data;

    public InvoiceResponse(Invoice invoice, String data) {
        this.invoice = invoice;
        this.data = data;
    }


	public Invoice getInvoice() {
        return invoice;
    }

    public String getData() {
        return data;
    }
    
    
}
