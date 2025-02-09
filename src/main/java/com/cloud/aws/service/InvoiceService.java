package com.cloud.aws.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.aws.model.Invoice;
import com.cloud.aws.repository.InvoiceRepo;

@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepo invoiceRepo;

    public Optional<Invoice> getInvoice(Long id) {
        return invoiceRepo.findById(id);
    }
}