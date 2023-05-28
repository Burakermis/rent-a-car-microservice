package com.kodlamaio.invoiceservice.repository;

import com.kodlamaio.invoiceservice.entities.Invoice;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends ElasticsearchRepository<Invoice, UUID> {
    List<Invoice> findAll();
}
