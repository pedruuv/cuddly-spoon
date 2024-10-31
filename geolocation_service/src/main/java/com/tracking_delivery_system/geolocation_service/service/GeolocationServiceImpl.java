package com.tracking_delivery_system.geolocation_service.service;

import com.tracking_delivery_system.geolocation_service.model.Coordinates;
import com.tracking_delivery_system.geolocation_service.model.Geolocation;
import com.tracking_delivery_system.geolocation_service.model.DeliveryRoute;
import com.tracking_delivery_system.geolocation_service.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeolocationServiceImpl implements GeolocationService {
    private static final String TRACKING_DELIVERY_TOPIC = "geolocate-delivery-route";

    private final CoordinatesService coordinatesService;
    private final RouteService routeService;
    private final KafkaProducer kafkaProducer;

    @Override
    public void calculateRoute(Geolocation geolocation) {
        try {
            Coordinates origin = coordinatesService.getCoordinatesForLocation(geolocation.origin());
            Coordinates destination = coordinatesService.getCoordinatesForLocation(geolocation.destination());

            double distance = coordinatesService.getTotalDistance(origin, destination) / 1000;
            List<State> states = routeService.getRouteStates(origin, destination);

            DeliveryRoute update = createDeliveryRoute(geolocation, distance, states);
            kafkaProducer.sendGeolocationInfo(TRACKING_DELIVERY_TOPIC, update);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate route for delivery ID: " + geolocation.id(), e);
        }
    }

    private static DeliveryRoute createDeliveryRoute(Geolocation geolocation, double distance, List<State> states) {
        return new DeliveryRoute(geolocation.id(), geolocation.origin(), geolocation.destination(), distance, states);
    }
}
