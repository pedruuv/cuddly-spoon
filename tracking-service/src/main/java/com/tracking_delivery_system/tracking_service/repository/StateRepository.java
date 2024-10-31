package com.tracking_delivery_system.tracking_service.repository;

import com.tracking_delivery_system.tracking_service.model.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
    
}
