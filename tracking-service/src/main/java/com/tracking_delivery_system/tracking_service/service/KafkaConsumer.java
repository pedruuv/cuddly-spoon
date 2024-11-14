package com.tracking_delivery_system.tracking_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracking_delivery_system.tracking_service.model.Delivery;
import com.tracking_delivery_system.tracking_service.model.LocationUpdate;
import com.tracking_delivery_system.tracking_service.repository.DeliveryRouteUpdateRepository;
import com.tracking_delivery_system.tracking_service.repository.LocationUpdateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final DeliveryRouteUpdateRepository repository;
    private final LocationUpdateRepository locationUpdateRepository;
    private final DeliveryProgressSimulatorService simulator;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void checkPendingDeliveries(){
        log.info("checkPendingDeliveries() is being called");
        List<LocationUpdate> pendingDeliveries = locationUpdateRepository.findByRemainingDistanceGreaterThan(0);
        pendingDeliveries.forEach(delivery ->{
            Delivery pendingDelivery = repository.findById(delivery.getId()).orElse(null);

            simulator.simulateDeliverProgress(pendingDelivery);
        });
    }

    @KafkaListener(topics = "geolocate-delivery-route", groupId = "tracking_group")
    public void consumeNewDelivery(String newDelivery) {
        try {
            Delivery delivery = mapper.readValue(newDelivery, Delivery.class);

            repository.save(delivery);
            simulator.simulateDeliverProgress(delivery);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
