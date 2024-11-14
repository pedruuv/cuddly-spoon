package com.tracking_delivery_system.delivery_status_service.error;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String message) {
        super(message);
    }
}
