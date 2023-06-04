package com.kodlamaio.rentalservice.business.concretes;

import com.kodlamaio.commonpackage.events.invoice.InvoiceCreatedEvent;
import com.kodlamaio.commonpackage.events.rental.RentalCreatedEvent;
import com.kodlamaio.commonpackage.events.rental.RentalDeletedEvent;
import com.kodlamaio.commonpackage.utils.dto.CreateRentalPaymentRequest;
import com.kodlamaio.commonpackage.utils.dto.GetCarResponse;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.rentalservice.api.clients.CarClient;
import com.kodlamaio.rentalservice.business.abstracts.RentalService;
import com.kodlamaio.rentalservice.business.dto.requests.CreateRentalRequest;
import com.kodlamaio.rentalservice.business.dto.requests.UpdateRentalRequest;
import com.kodlamaio.rentalservice.business.dto.responses.CreateRentalResponse;
import com.kodlamaio.rentalservice.business.dto.responses.GetAllRentalsResponse;
import com.kodlamaio.rentalservice.business.dto.responses.GetRentalResponse;
import com.kodlamaio.rentalservice.business.dto.responses.UpdateRentalResponse;
import com.kodlamaio.rentalservice.business.rules.RentalBusinessRules;
import com.kodlamaio.rentalservice.entities.Rental;
import com.kodlamaio.rentalservice.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentalManager implements RentalService {
    private final RentalRepository repository;
    private final ModelMapperService mapper;
    private final RentalBusinessRules rules;
    private final KafkaProducer kafkaProducer;
    @Qualifier("com.kodlamaio.rentalservice.api.clients.CarClient")
    private final CarClient carClient;

    @Override
    public List<GetAllRentalsResponse> getAll() {
        var rentals = repository.findAll();
        var response = rentals
                .stream()
                .map(rental -> mapper.forResponse().map(rental, GetAllRentalsResponse.class))
                .toList();

        return response;
    }

    @Override
    public GetRentalResponse getById(UUID id) {
        rules.checkIfRentalExists(id);
        var rental = repository.findById(id).orElseThrow();
        var response = mapper.forResponse().map(rental, GetRentalResponse.class);

        return response;
    }

    @Override
    public CreateRentalResponse add(CreateRentalRequest request) {
        rules.ensureCarIsAvailable(request.getCarId());
        var rental = mapper.forRequest().map(request, Rental.class);
        rental.setId(null);
        rental.setTotalPrice(getTotalPrice(rental));
        rental.setRentedAt(LocalDate.now());

        GetCarResponse car = carClient.getById(request.getCarId());
        var paymentRequest = new CreateRentalPaymentRequest();
        mapper.forRequest().map(request.getGetCardInfo(), paymentRequest);
        paymentRequest.setPrice(getTotalPrice(rental));
        // Feign client kullanarak ödeme gerçekleştiriliyor
        rules.ensurePayment(paymentRequest);
        repository.save(rental);

        // Fatura oluşturuluyor
        sendKafkaInvoiceCreatedEvent(rental, car, request.getGetCardInfo().getCardHolder());

        sendKafkaRentalCreatedEvent(request.getCarId());
        var response = mapper.forResponse().map(rental, CreateRentalResponse.class);

        return response;
    }

    @Override
    public UpdateRentalResponse update(UUID id, UpdateRentalRequest request) {
        rules.checkIfRentalExists(id);
        var rental = mapper.forRequest().map(request, Rental.class);
        rental.setId(id);
        rental.setTotalPrice(getTotalPrice(rental));
        repository.save(rental);
        var response = mapper.forResponse().map(rental, UpdateRentalResponse.class);

        return response;
    }

    @Override
    public void delete(UUID id) {
        rules.checkIfRentalExists(id);
        sendKafkaRentalDeletedEvent(id);
        repository.deleteById(id);
    }

    private double getTotalPrice(Rental rental) {
        return rental.getDailyPrice() * rental.getRentedForDays();
    }

    private void sendKafkaRentalCreatedEvent(UUID carId) {
        kafkaProducer.sendMessage(new RentalCreatedEvent(carId), "rental-created");
    }

    private void sendKafkaRentalDeletedEvent(UUID id) {
        var carId = repository.findById(id).orElseThrow().getCarId();
        kafkaProducer.sendMessage(new RentalDeletedEvent(carId), "rental-deleted");
    }
    private void sendKafkaInvoiceCreatedEvent(Rental rental, GetCarResponse carResponse, String cardHolder){
        InvoiceCreatedEvent event = new InvoiceCreatedEvent();
        event.setCardHolder(cardHolder);
        event.setModelName(carResponse.getModelName());
        event.setBrandName(carResponse.getBrandName());
        event.setPlate(carResponse.getPlate());
        event.setModelYear(carResponse.getModelYear());
        event.setDailyPrice(rental.getDailyPrice());
        event.setTotalPrice(rental.getTotalPrice());
        event.setRentedForDays(rental.getRentedForDays());
        event.setRentedAt(rental.getRentedAt());
        kafkaProducer.sendMessage(event,"invoice-created");
    }
}
