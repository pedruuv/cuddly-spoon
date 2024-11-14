package com.tracking_delivery_system.geolocation_service.model;

import java.util.List;
import java.util.UUID;

public record DeliveryRoute(UUID id, String origin, String destination, double distance, List<State> states) {
}
