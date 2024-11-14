package com.tracking_delivery_system.delivery_status_service.service;

import com.tracking_delivery_system.delivery_status_service.error.NotFoundException;
import com.tracking_delivery_system.delivery_status_service.model.Deliver;
import com.tracking_delivery_system.delivery_status_service.model.DeliveryStatus;
import com.tracking_delivery_system.delivery_status_service.model.LocationUpdate;
import com.tracking_delivery_system.delivery_status_service.model.StatusUpdate;
import com.tracking_delivery_system.delivery_status_service.repository.DeliverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryStatusUpdaterTest {
    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private DeliverRepository repository;
    @InjectMocks
    private DeliveryStatusUpdater deliveryStatusUpdater;

    private Deliver deliver;
    private LocationUpdate locationUpdate;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        deliver = new Deliver();
        deliver.setId(UUID.randomUUID());
        deliver.setStatus(DeliveryStatus.AWAITING_PICKUP);
        deliver.setCurrentLocation("City A");
    }

    @Test
    void updateBasedDistance_ShouldUpdateStatusAndLocation_WhenStatusChanges(){
        when(repository.findById(deliver.getId())).thenReturn(Optional.of(deliver));

        locationUpdate = new LocationUpdate(deliver.getId(), "State B", "City B", 100, 95);

        deliveryStatusUpdater.updateBasedDistance(locationUpdate);

        assertEquals(DeliveryStatus.PICKED_UP, deliver.getStatus());

        verify(kafkaProducer, times(1)).sendUpdate(eq("delivery-status"), any(StatusUpdate.class));


        assertEquals("City B", deliver.getCurrentLocation());

        verify(repository, times(1)).save(deliver);
    }

    @Test
    void updateBasedDistance_ShouldNotSendStatus_WhenNoChangeInStatusOrLocation(){
        when(repository.findById(deliver.getId())).thenReturn(Optional.of(deliver));

        locationUpdate = new LocationUpdate(deliver.getId(), "State A", "City A", 100, 100);

        deliveryStatusUpdater.updateBasedDistance(locationUpdate);

        assertEquals(DeliveryStatus.AWAITING_PICKUP, deliver.getStatus());
        assertEquals("City A", deliver.getCurrentLocation());

        verify(kafkaProducer, never()).sendUpdate(anyString(), any(StatusUpdate.class));

        verify(repository, never()).save(deliver);
    }

    @Test
    void updateBasedDistance_ShouldThrowNotFoundException_WhenDeliverNotFound(){
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        locationUpdate = new LocationUpdate(UUID.randomUUID(), "State A", "City A", 100, 50);

        assertThrows(NotFoundException.class, () -> deliveryStatusUpdater.updateBasedDistance(locationUpdate));

        verify(kafkaProducer, never()).sendUpdate(anyString(), any(StatusUpdate.class));
        verify(repository, never()).save(any(Deliver.class));
    }

}