package com.tracking_delivery_system.geolocation_service.service;

import com.tracking_delivery_system.geolocation_service.model.Coordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoordinatesService {
    private final APIRouteService apiRouteService;

    @Value("${geolocation.api.url}")
    private String geolocationApiUrl;

    @Value("${routing.service.api.url}")
    private String routeApiUrl;

    public Coordinates getCoordinatesForLocation(String cityName) {
        String url = String.format(geolocationApiUrl, cityName);
        return apiRouteService.fetchCoordinates(url);
    }

    public double getTotalDistance(Coordinates origin, Coordinates destination) {
        String url = String.format(routeApiUrl,
                origin.longitude(), origin.latitude(), destination.longitude(), destination.latitude());
        return apiRouteService.fetchTotalDistance(url);
    }
}
