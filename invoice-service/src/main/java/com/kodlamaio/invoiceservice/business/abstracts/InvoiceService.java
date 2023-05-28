package com.kodlamaio.invoiceservice.business.abstracts;

import com.kodlamaio.invoiceservice.business.dto.requests.UpdateInvoiceRequest;
import com.kodlamaio.invoiceservice.business.dto.responses.CreateInvoiceResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.GetAllInvoicesResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.GetInvoiceResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.UpdateInvoiceResponse;
import com.kodlamaio.invoiceservice.entities.Invoice;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    List<GetAllInvoicesResponse> getAll();
    GetInvoiceResponse getById(UUID id);
    CreateInvoiceResponse add(Invoice invoice);
    UpdateInvoiceResponse update(UpdateInvoiceRequest updateInvoiceRequest);
    void delete(UUID id);
}
