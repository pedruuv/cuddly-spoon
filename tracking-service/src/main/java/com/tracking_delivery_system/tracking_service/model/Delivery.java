package com.tracking_delivery_system.tracking_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Delivery {
    @Id
    private UUID id;
    private String origin;
    private String destination;
    private double distance;
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<State> states;

    public Delivery(UUID id, String origin, String destination, double distance) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
    }

    private void setStates(List<State> states){
        this.states = states;
        states.forEach(state -> state.setDelivery(this));
    }
}
