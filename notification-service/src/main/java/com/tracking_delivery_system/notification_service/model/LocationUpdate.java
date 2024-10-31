package com.tracking_delivery_system.notification_service.model;

import java.util.UUID;

public record LocationUpdate(UUID id, String currentState, String currentCity, double totalDistance, double remainingDistance) {
}
