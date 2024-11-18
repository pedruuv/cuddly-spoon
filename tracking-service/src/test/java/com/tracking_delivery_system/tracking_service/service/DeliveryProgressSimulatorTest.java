package com.tracking_delivery_system.tracking_service.service;

import com.tracking_delivery_system.tracking_service.model.Delivery;
import com.tracking_delivery_system.tracking_service.model.LocationUpdate;
import com.tracking_delivery_system.tracking_service.model.State;
import com.tracking_delivery_system.tracking_service.repository.LocationUpdateRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryProgressSimulatorTest {
    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private LocationUpdateRepository locationUpdateRepository;
    @Mock
    private ScheduledFuture<?> scheduledFuture;
    @InjectMocks
    private DeliveryProgressSimulator deliveryProgressSimulator;

    private Delivery testDelivery;
    private LocationUpdate locationUpdate;

    @SneakyThrows
    @BeforeEach
    void setUp(){
        UUID id = UUID.randomUUID();
        testDelivery = new Delivery(id, "Origin City", "Destination City", 1000);
        State stateA = new State(1L, "State A", "City A", testDelivery);
        State stateB = new State(2L, "State B", "City B", testDelivery);

        Method setStates = Delivery.class.getDeclaredMethod("setStates", List.class);
        setStates.setAccessible(true);
        setStates.invoke(testDelivery, Arrays.asList(stateA, stateB));

        locationUpdate = new LocationUpdate(id, "State 0", "City 0", 1000, 900);
    }

    @Test
    void simulateDeliveryProgress_ShouldScheduleTask(){
        when(locationUpdateRepository.findById(locationUpdate.getId())).thenReturn(Optional.of(locationUpdate));

        deliveryProgressSimulator.simulateDeliverProgress(testDelivery);

        verify(kafkaProducer, atLeastOnce()).sendUpdate(anyString(), any(LocationUpdate.class));
    }

    @Test
    void simulateDeliveryProgress_ShouldCancelExistingTask(){
        scheduledFuture = mock(ScheduledFuture.class);
        doAnswer(i -> null).when(kafkaProducer).sendUpdate(anyString(), any(LocationUpdate.class));

        deliveryProgressSimulator.simulateDeliverProgress(testDelivery);
        verify(scheduledFuture, never()).cancel(false);
    }

    @Test
    void simulateDeliveryProgress_ShouldHandleKafkaProducerException(){
        when(locationUpdateRepository.findById(any(UUID.class))).thenReturn(Optional.of(locationUpdate));

        doThrow(new RuntimeException("Kafka error")).when(kafkaProducer).sendUpdate(anyString(), any(LocationUpdate.class));

        assertDoesNotThrow(() -> deliveryProgressSimulator.simulateDeliverProgress(testDelivery));

        verify(kafkaProducer, atLeastOnce()).sendUpdate(anyString(), any(LocationUpdate.class));

    }

    @SneakyThrows
    @Test
    void simulateDeliveryProgress_ShouldHandleEmptyStateList(){
        Method setStates = Delivery.class.getDeclaredMethod("setStates", List.class);
        setStates.setAccessible(true);
        setStates.invoke(testDelivery, List.of());

        assertThrows(IllegalArgumentException.class, () -> deliveryProgressSimulator.simulateDeliverProgress(testDelivery));

        verify(kafkaProducer, never()).sendUpdate(anyString(), any(LocationUpdate.class));
    }
}