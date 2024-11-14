package com.tracking_delivery_system.tracking_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class State {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String cityName;
    @ManyToOne
    @JoinColumn(name = "delivery_id")
    @ToString.Exclude
    private Delivery delivery;
}
