package com.kodlamaio.invoiceservice.business.concretes;

import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.invoiceservice.business.abstracts.InvoiceService;
import com.kodlamaio.invoiceservice.business.dto.requests.create.CreateInvoiceRequest;
import com.kodlamaio.invoiceservice.business.dto.requests.update.UpdateInvoiceRequest;
import com.kodlamaio.invoiceservice.business.dto.responses.create.CreateInvoiceResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetAllInvoicesResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetInvoiceResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.update.UpdateInvoiceResponse;
import com.kodlamaio.invoiceservice.business.rules.InvoiceBusinessRules;
import com.kodlamaio.invoiceservice.entities.Invoice;
import com.kodlamaio.invoiceservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceManager implements InvoiceService {
    private final InvoiceRepository repository;
    private final ModelMapperService mapper;
    private final InvoiceBusinessRules rules;
    @Override
    public List<GetAllInvoicesResponse> getAll() {
        List<Invoice> invoices = repository.findAll();
        List<GetAllInvoicesResponse> getAllInvoicesResponses = invoices
                .stream()
                .map(invoice -> mapper.forResponse().map(invoice, GetAllInvoicesResponse.class))
                .collect(Collectors.toList());
        return getAllInvoicesResponses;
    }

    @Override
    public GetInvoiceResponse getById(UUID id) {
        rules.checkIfInvoiceExists(id);
        Invoice invoice = repository.findById(id).orElseThrow();
        GetInvoiceResponse getInvoiceResponse = mapper.forResponse().map(invoice, GetInvoiceResponse.class);

        return getInvoiceResponse;
    }

    @Override
    public CreateInvoiceResponse add(CreateInvoiceRequest request) {
        Invoice invoice = mapper.forRequest().map(request, Invoice.class);
        invoice.setId(UUID.randomUUID());
        invoice.setTotalPrice(getTotalPrice(invoice));
        repository.save(invoice);
        CreateInvoiceResponse createInvoiceResponse =
                mapper.forResponse().map(invoice, CreateInvoiceResponse.class);
        return createInvoiceResponse;
    }

    @Override
    public UpdateInvoiceResponse update(UpdateInvoiceRequest updateInvoiceRequest) {
        rules.checkIfInvoiceExists(updateInvoiceRequest.getId());
        Invoice invoice = mapper.forRequest().map(updateInvoiceRequest, Invoice.class);
        invoice.setId(updateInvoiceRequest.getId());
        repository.save(invoice);
        UpdateInvoiceResponse response = mapper.forResponse().map(invoice, UpdateInvoiceResponse.class);
        return response;
    }

    @Override
    public void delete(UUID id) {
        rules.checkIfInvoiceExists(id);
        repository.deleteById(id);
    }
    private double getTotalPrice(Invoice invoice) {
        return invoice.getDailyPrice() * invoice.getRentedForDays();
    }
}
