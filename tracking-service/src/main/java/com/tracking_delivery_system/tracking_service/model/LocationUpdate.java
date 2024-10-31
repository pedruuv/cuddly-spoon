package com.tracking_delivery_system.tracking_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "location-update")
public class LocationUpdate {
    @Id
    private UUID id;
    private String currentState;
    private String currentCity;
    private double totalDistance;
    private double remainingDistance;
}
