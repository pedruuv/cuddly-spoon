package com.tracking_delivery_system.delivery_status_service.service;

import com.tracking_delivery_system.delivery_status_service.dto.NewDeliverDto;
import com.tracking_delivery_system.delivery_status_service.model.Deliver;
import com.tracking_delivery_system.delivery_status_service.model.StatusUpdate;
import com.tracking_delivery_system.delivery_status_service.repository.DeliverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliverServiceImplTest {
    @Mock
    private DeliverRepository repository;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private DeliverServiceImpl service;

    private NewDeliverDto dto;
    private Deliver deliver;

    @BeforeEach
    void setUp(){
        dto = new NewDeliverDto("Test Product", "John Doe", "johndoe@example.com", "+123456789", "Origin City", "Destination City");
        deliver = new Deliver(dto.productName(), dto.clientName(), dto.clientEmail(), dto.clientPhone(), dto.origin(), dto.destination(), dto.origin());
    }

    @Test
    void createNewDeliver_ShouldSaveDeliverAndSendKafkaMessage() {
        when(repository.save(any(Deliver.class))).thenReturn(deliver);

        Deliver result = service.createNewDeliver(dto);

        doNothing().when(kafkaProducer).sendUpdate(eq("new-delivery"), any(Deliver.class));
        doNothing().when(kafkaProducer).sendUpdate(eq("delivery-status"), any(StatusUpdate.class));

        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        assertEquals("John Doe", result.getClientName());
        assertEquals("Origin City", result.getOrigin());
        assertEquals("Destination City", result.getDestination());
    }

    @Test
    void createNewDelivery_ShouldThrownException_WhenRepositorySaveFails(){
        when(repository.save(any(Deliver.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> service.createNewDeliver(dto));

        verify(repository, times(1)).save(any(Deliver.class));
        verify(kafkaProducer, never()).sendUpdate(anyString(), any());
    }

    @Test
    void createNewDelivery_ShouldThrownRuntimeException_WhenKafkaProducerFails(){
        doThrow(new RuntimeException("Kafka error")).when(kafkaProducer).sendUpdate("new-delivery", deliver);

        assertThrows(RuntimeException.class, () -> service.createNewDeliver(dto));

        verify(repository, times(1)).save(any(Deliver.class));
        verify(kafkaProducer, times(1)).sendUpdate("new-delivery", deliver);
        verify(kafkaProducer, never()).sendUpdate(eq("delivery-status"), any(StatusUpdate.class));

    }
}