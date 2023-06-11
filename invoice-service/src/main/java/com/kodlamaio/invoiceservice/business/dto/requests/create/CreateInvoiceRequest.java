package com.kodlamaio.invoiceservice.business.dto.requests.create;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {
    private String cardHolder;
    private String modelName;
    private String brandName;
    private String plate;
    private int modelYear;
    private double dailyPrice;
    private int rentedForDays;
    private LocalDateTime rentedAt;
}
