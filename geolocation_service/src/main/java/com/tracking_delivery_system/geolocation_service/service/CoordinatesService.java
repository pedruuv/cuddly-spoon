package com.tracking_delivery_system.geolocation_service.service;

import com.tracking_delivery_system.geolocation_service.model.Coordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoordinatesService {
    private final APIRouteService apiRouteService;

    public Coordinates getCoordinatesForLocation(String cityName) {
        String url = String.format("https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1", cityName);
        return apiRouteService.fetchCoordinates(url);
    }

    public double getTotalDistance(Coordinates origin, Coordinates destination) {
        String url = String.format("https://router.project-osrm.org/route/v1/driving/%s,%s;%s,%s",
                origin.longitude(), origin.latitude(), destination.longitude(), destination.latitude());
        return apiRouteService.fetchTotalDistance(url);
    }
}
