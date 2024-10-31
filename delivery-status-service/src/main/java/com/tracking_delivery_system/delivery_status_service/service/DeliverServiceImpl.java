package com.tracking_delivery_system.delivery_status_service.service;

import com.tracking_delivery_system.delivery_status_service.dto.NewDeliverDto;
import com.tracking_delivery_system.delivery_status_service.model.Deliver;
import com.tracking_delivery_system.delivery_status_service.model.StatusUpdate;
import com.tracking_delivery_system.delivery_status_service.repository.DeliverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliverServiceImpl implements DeliverService{
    private final DeliverRepository repository;
    private final KafkaProducer kafkaProducer;


    @Override
    public Deliver createNewDeliver(NewDeliverDto deliverDto) {
        Deliver deliver = new Deliver(deliverDto.productName(), deliverDto.clientName(), deliverDto.clientEmail(), deliverDto.clientPhone(), deliverDto.origin(), deliverDto.destination(), deliverDto.origin());

        repository.save(deliver);

        kafkaProducer.sendUpdate("new-delivery", deliver);
        StatusUpdate status = new StatusUpdate(deliver.getId(), deliver.getStatus().name());
        kafkaProducer.sendUpdate("delivery-status", status);
        return deliver;
    }
}
