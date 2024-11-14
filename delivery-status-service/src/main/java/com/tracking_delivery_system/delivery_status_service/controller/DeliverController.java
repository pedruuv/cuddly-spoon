package com.tracking_delivery_system.delivery_status_service.controller;

import com.tracking_delivery_system.delivery_status_service.dto.NewDeliverDto;
import com.tracking_delivery_system.delivery_status_service.model.Deliver;
import com.tracking_delivery_system.delivery_status_service.model.StatusUpdate;
import com.tracking_delivery_system.delivery_status_service.repository.DeliverRepository;
import com.tracking_delivery_system.delivery_status_service.service.DeliverService;
import com.tracking_delivery_system.delivery_status_service.service.KafkaProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliverController {
    private final DeliverService service;

    @PostMapping
    public ResponseEntity<Deliver> newDelivery(@Valid @RequestBody NewDeliverDto newDeliver){
        Deliver deliver = service.createNewDeliver(newDeliver);
        return ResponseEntity.status(HttpStatus.CREATED).body(deliver);
    }
}
