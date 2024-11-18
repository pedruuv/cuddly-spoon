package com.tracking_delivery_system.geolocation_service.service;

import com.tracking_delivery_system.geolocation_service.model.Coordinates;
import com.tracking_delivery_system.geolocation_service.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final APIRouteService externalApiService;

    @Value("${location.state.api.url}")
    private String statesApiUrl;

    public List<State> getRouteStates(Coordinates origin, Coordinates destination) {
        String url = String.format(statesApiUrl,
                origin.longitude(), origin.latitude(), destination.longitude(), destination.latitude());
        return externalApiService.fetchStates(url);
    }
}
