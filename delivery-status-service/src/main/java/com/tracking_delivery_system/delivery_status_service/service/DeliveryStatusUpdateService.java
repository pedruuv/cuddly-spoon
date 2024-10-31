package com.tracking_delivery_system.delivery_status_service.service;

import com.tracking_delivery_system.delivery_status_service.model.LocationUpdate;

public interface DeliveryStatusUpdateService {
    void updateBasedDistance(LocationUpdate locationUpdate);
}
