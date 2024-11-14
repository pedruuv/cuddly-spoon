package com.tracking_delivery_system.tracking_service.repository;

import com.tracking_delivery_system.tracking_service.model.Delivery;
import com.tracking_delivery_system.tracking_service.model.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRouteUpdateRepository extends JpaRepository<Delivery, UUID> {
}

