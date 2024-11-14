package com.tracking_delivery_system.tracking_service.service;

import com.tracking_delivery_system.tracking_service.model.Delivery;
import com.tracking_delivery_system.tracking_service.model.LocationUpdate;
import com.tracking_delivery_system.tracking_service.model.State;
import com.tracking_delivery_system.tracking_service.repository.DeliveryRouteUpdateRepository;
import com.tracking_delivery_system.tracking_service.repository.LocationUpdateRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {
    @Mock
    private DeliveryRouteUpdateRepository deliveryRouteUpdateRepository;
    @Mock
    private LocationUpdateRepository locationUpdateRepository;
    @Mock
    private DeliveryProgressSimulatorService simulator;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Test
    void checkPendingDeliveries_ShouldSimulateProgressForPendingDeliveries() {
        UUID id = UUID.randomUUID();
        LocationUpdate pendingLocationUpdate = new LocationUpdate(id, "IN_TRANSIT", "City A", 1000, 500);

        Delivery pendingDelivery = new Delivery(id, "Origin City", "Destination City", 1000, List.of(new State(1L, "State A", "City A", null), new State(2L, "State B", "City B", null)));

        when(locationUpdateRepository.findByRemainingDistanceGreaterThan(0)).thenReturn(List.of(pendingLocationUpdate));
        when(deliveryRouteUpdateRepository.findById(pendingDelivery.getId())).thenReturn(Optional.of(pendingDelivery));

        pendingDelivery.getStates().forEach(state -> state.setDelivery(pendingDelivery));

        kafkaConsumer.checkPendingDeliveries();

        verify(simulator, times(1)).simulateDeliverProgress(pendingDelivery);
    }

    @Test
    void checkPendingDeliveries_ShouldNotSimulateProgress_WhenNoPendingDeliveriesFound() {
        when(locationUpdateRepository.findByRemainingDistanceGreaterThan(0)).thenReturn(List.of());
        kafkaConsumer.checkPendingDeliveries();
        verify(simulator, never()).simulateDeliverProgress(any());
    }

    @SneakyThrows
    @Test
    void consumeNewDelivery_ShouldSaveNewDelivery() {
        UUID id = UUID.randomUUID();
        String jsonMessage = "{\"id\":\"" + id + "\", \"distance\":1000, \"origin\":\"Origin City\", \"destination\":\"Destination City\", \"states\":[{\"id\":1, \"name\":\"State A\", \"cityName\":\"City A\"}, {\"id\":2, \"name\":\"State B\", \"cityName\":\"City B\"}]}";

        Delivery testDelivery = new Delivery(id, "Origin City", "Destination City", 1000);
        State stateA = new State(1L, "State A", "City A", testDelivery);
        State stateB = new State(2L, "State B", "City B", testDelivery);

        Method setStates = Delivery.class.getDeclaredMethod("setStates", List.class);
        setStates.setAccessible(true);
        setStates.invoke(testDelivery, Arrays.asList(stateA, stateB));

        kafkaConsumer.consumeNewDelivery(jsonMessage);

        verify(simulator).simulateDeliverProgress(any(Delivery.class));
        verify(deliveryRouteUpdateRepository, times(1)).save(any(Delivery.class));

    }
}