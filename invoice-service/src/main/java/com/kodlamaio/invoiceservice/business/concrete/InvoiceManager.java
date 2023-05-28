package com.kodlamaio.invoiceservice.business.concrete;

import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.invoiceservice.business.abstracts.InvoiceService;
import com.kodlamaio.invoiceservice.business.dto.requests.UpdateInvoiceRequest;
import com.kodlamaio.invoiceservice.business.dto.responses.CreateInvoiceResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.GetAllInvoicesResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.GetInvoiceResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.UpdateInvoiceResponse;
import com.kodlamaio.invoiceservice.entities.Invoice;
import com.kodlamaio.invoiceservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceManager implements InvoiceService {
    private final InvoiceRepository repository;
    private final ModelMapperService mapper;
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
       // invoiceBusinessRules.checkIfInvoiceExists(id);
        Invoice invoice = repository.findById(id).orElseThrow();
        GetInvoiceResponse getInvoiceResponse = mapper.forResponse().map(invoice, GetInvoiceResponse.class);

        return getInvoiceResponse;
    }

    @Override
    public CreateInvoiceResponse add(Invoice invoice) {
       // invoice.setRentedAt(LocalDate.now());
        invoice.setId(UUID.randomUUID());
        invoice.setTotalPrice(getTotalPrice(invoice));

        repository.save(invoice);

        CreateInvoiceResponse createInvoiceResponse =
                mapper.forResponse().map(invoice, CreateInvoiceResponse.class);
        return createInvoiceResponse;
    }

    @Override
    public UpdateInvoiceResponse update(UpdateInvoiceRequest updateInvoiceRequest) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }
    private double getTotalPrice(Invoice invoice) {
        return invoice.getDailyPrice() * invoice.getRentedForDays();
    }
}
