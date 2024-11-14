package com.tracking_delivery_system.geolocation_service.service;

import com.tracking_delivery_system.geolocation_service.model.Geolocation;

public interface GeolocationService {
    void calculateRoute(Geolocation geolocation);
}
