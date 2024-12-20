package com.tracking_delivery_system.delivery_status_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "new_deliveries")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deliver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String productName;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String origin;
    private String destination;
    private String currentLocation;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    public Deliver(String productName, String clientName, String clientEmail, String clientPhone, String origin, String destination, String currentLocation) {
        this.productName = productName;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.clientPhone = clientPhone;
        this.origin = origin;
        this.destination = destination;
        this.currentLocation = currentLocation;
        this.status = DeliveryStatus.CREATED;
    }
}
