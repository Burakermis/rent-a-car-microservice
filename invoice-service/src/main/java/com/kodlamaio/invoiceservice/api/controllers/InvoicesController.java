package com.kodlamaio.invoiceservice.api.controllers;

import com.kodlamaio.invoiceservice.business.abstracts.InvoiceService;
import com.kodlamaio.invoiceservice.business.dto.requests.create.CreateInvoiceRequest;
import com.kodlamaio.invoiceservice.business.dto.requests.update.UpdateInvoiceRequest;
import com.kodlamaio.invoiceservice.business.dto.responses.create.CreateInvoiceResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetAllInvoicesResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetInvoiceResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.update.UpdateInvoiceResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/invoices")
public class InvoicesController {
    private final InvoiceService service;

//    MongoDB collection oluşması için sadece ilk çalıştırmada kullanılıyor!!!
//    @PostConstruct
//    public void createDb(){
//        service.add(new Invoice());
//    }
    @GetMapping
    public List<GetAllInvoicesResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public GetInvoiceResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateInvoiceResponse add(@Valid @RequestBody CreateInvoiceRequest request) {
        return service.add(request);
    }

    @PutMapping
    public UpdateInvoiceResponse update(@Valid @RequestBody UpdateInvoiceRequest request) {
        return service.update(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

}
