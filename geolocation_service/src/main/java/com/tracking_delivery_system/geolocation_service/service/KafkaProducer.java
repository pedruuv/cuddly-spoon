package com.tracking_delivery_system.geolocation_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public <T> void sendGeolocationInfo(String topic, T data){
        kafkaTemplate.send(topic, data);
    }
}
