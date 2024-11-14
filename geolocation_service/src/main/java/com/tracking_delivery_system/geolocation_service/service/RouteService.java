package com.tracking_delivery_system.geolocation_service.service;

import com.tracking_delivery_system.geolocation_service.model.Coordinates;
import com.tracking_delivery_system.geolocation_service.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final APIRouteService externalApiService;

    public List<State> getRouteStates(Coordinates origin, Coordinates destination) {
        String url = String.format("http://router.project-osrm.org/route/v1/driving/%s,%s;%s,%s?geometries=geojson&steps=true",
                origin.longitude(), origin.latitude(), destination.longitude(), destination.latitude());
        return externalApiService.fetchStates(url);
    }
}
