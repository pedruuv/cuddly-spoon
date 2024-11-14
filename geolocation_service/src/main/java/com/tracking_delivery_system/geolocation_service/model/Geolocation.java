package com.tracking_delivery_system.geolocation_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Geolocation(UUID id, String origin, String destination) {
}
