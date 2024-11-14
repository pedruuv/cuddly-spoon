package com.tracking_delivery_system.geolocation_service.service;

import com.tracking_delivery_system.geolocation_service.model.Coordinates;
import com.tracking_delivery_system.geolocation_service.model.DeliveryRoute;
import com.tracking_delivery_system.geolocation_service.model.Geolocation;
import com.tracking_delivery_system.geolocation_service.model.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeolocationServiceImplTest {
    private static final String TRACKING_DELIVERY_TOPIC = "geolocate-delivery-route";

    @Mock
    private CoordinatesService coordinatesService;
    @Mock
    private RouteService routeService;
    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private GeolocationServiceImpl geolocationService;

    private Geolocation testGeolocation;
    private Coordinates originCoordinates;
    private Coordinates destinationCoordinates;

    @BeforeEach
    void setUp() {
        testGeolocation = new Geolocation(UUID.randomUUID(), "Origin City", "Destination City");
        originCoordinates = new Coordinates("10", "24");
        destinationCoordinates = new Coordinates("30", "40");
    }

    @Test
    void calculateRoute_ShouldSendGeolocationInfo_WhenValidGeolocationIsProvided() {
        double distance = 1500;
        List<State> states = List.of(new State("State1", "City1"), new State("State2", "City2"));

        when(coordinatesService.getCoordinatesForLocation(testGeolocation.origin())).thenReturn(originCoordinates);
        when(coordinatesService.getCoordinatesForLocation(testGeolocation.destination())).thenReturn(destinationCoordinates);
        when(coordinatesService.getTotalDistance(originCoordinates, destinationCoordinates)).thenReturn(distance);
        when(routeService.getRouteStates(originCoordinates, destinationCoordinates)).thenReturn(states);

        geolocationService.calculateRoute(testGeolocation);

        DeliveryRoute expectedRoute = new DeliveryRoute(testGeolocation.id(), "Origin City", "Destination City", distance / 1000, states);
        verify(kafkaProducer).sendGeolocationInfo(TRACKING_DELIVERY_TOPIC, expectedRoute);
    }

    @Test
    void calculateRoute_ShouldThrownException_WhenOriginCoordinatesNotFound() {
        when(coordinatesService.getCoordinatesForLocation(testGeolocation.origin())).thenThrow(new RuntimeException("Origin coordinates not found"));

        assertThrows(RuntimeException.class, () -> geolocationService.calculateRoute(testGeolocation));
    }

    @Test
    void calculateRoute_ShouldThrownException_WhenDestinationCoordinatesNotFound(){
        when(coordinatesService.getCoordinatesForLocation(testGeolocation.origin())).thenReturn(originCoordinates);
        when(coordinatesService.getCoordinatesForLocation(testGeolocation.destination())).thenThrow(new RuntimeException("Destination coordinates not found"));

        assertThrows(RuntimeException.class, () -> geolocationService.calculateRoute(testGeolocation));
    }

    @Test
    void calculateRoute_ShouldThrownRuntimeException_WhenDistanceCalculationFails(){
        when(coordinatesService.getTotalDistance(originCoordinates, destinationCoordinates)).thenThrow(new RuntimeException("Distance calculation failed"));

        assertThrows(RuntimeException.class, () -> geolocationService.calculateRoute(testGeolocation));
    }

    @Test
    void calculateRoute_ShouldThrowRuntimeException_WhenRouteServiceFails() {
        when(coordinatesService.getCoordinatesForLocation(testGeolocation.origin())).thenReturn(originCoordinates);
        when(coordinatesService.getCoordinatesForLocation(testGeolocation.destination())).thenReturn(destinationCoordinates);
        when(coordinatesService.getTotalDistance(originCoordinates, destinationCoordinates)).thenReturn(1500.0);

        when(routeService.getRouteStates(originCoordinates, destinationCoordinates)).thenThrow(new RuntimeException("Route calculation failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> geolocationService.calculateRoute(testGeolocation));
        assertTrue(exception.getMessage().contains("Failed to calculate route for delivery ID"));
    }

    @Test
    void calculateRoute_ShouldThrownRuntimeException_WhenKafkaProducerFails(){
        doThrow(new RuntimeException("Kafka producer error")).when(kafkaProducer).sendGeolocationInfo(anyString(), any(DeliveryRoute.class));

        assertThrows(RuntimeException.class, () -> geolocationService.calculateRoute(testGeolocation));

    }
}