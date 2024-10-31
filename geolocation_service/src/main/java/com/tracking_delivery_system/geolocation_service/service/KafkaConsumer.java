package com.tracking_delivery_system.geolocation_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracking_delivery_system.geolocation_service.model.Geolocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final GeolocationService geolocationService;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "new-delivery", groupId = "geolocation-group")
    public void consumeNewDelivery(String newDelivery){
        try {
            Geolocation geolocation = mapper.readValue(newDelivery, Geolocation.class);
            geolocationService.calculateRoute(geolocation);
        } catch (JsonProcessingException e) {
            log.error("Error processing new delivery message: {}", newDelivery, e);
        }
    }
}
