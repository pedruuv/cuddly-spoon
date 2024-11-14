package com.tracking_delivery_system.delivery_status_service.controller;

import com.tracking_delivery_system.delivery_status_service.dto.NewDeliverDto;
import com.tracking_delivery_system.delivery_status_service.service.DeliverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DeliverControllerTest {
    @Mock
    private DeliverService service;
    @InjectMocks
    private DeliverController controller;

    private NewDeliverDto invalidDto;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        invalidDto = new NewDeliverDto(null, "John Doe", "johndoe@example.com", "+123456789", "Origin City", "Destination City");
    }

    @Test
    void createNewDeliver_ShouldReturnBadRequest_WhenDtoIsInvalid(){
        when(service.createNewDeliver(invalidDto)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Dto Fields"));

        assertThrows(ResponseStatusException.class, () -> controller.newDelivery(invalidDto));
    }

}