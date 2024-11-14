package com.tracking_delivery_system.delivery_status_service.service;

import com.tracking_delivery_system.delivery_status_service.dto.NewDeliverDto;
import com.tracking_delivery_system.delivery_status_service.model.Deliver;

public interface DeliverService {
    Deliver createNewDeliver(NewDeliverDto deliverDto);
}
