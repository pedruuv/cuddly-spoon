package com.tracking_delivery_system.delivery_status_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracking_delivery_system.delivery_status_service.model.LocationUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final DeliveryStatusUpdateService statusUpdater;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "location-update", groupId = "delivery-status")
    public void consumeLocationUpdate(String trackingUpdate) {
        try {
            LocationUpdate locationUpdate = mapper.readValue(trackingUpdate, LocationUpdate.class);
            statusUpdater.updateBasedDistance(locationUpdate);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
