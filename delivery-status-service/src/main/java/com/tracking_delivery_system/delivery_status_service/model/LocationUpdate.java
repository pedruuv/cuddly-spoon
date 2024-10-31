package com.tracking_delivery_system.delivery_status_service.model;

import java.util.UUID;

public record LocationUpdate(UUID id, String currentState, String currentCity, double totalDistance, double remainingDistance) {
}
