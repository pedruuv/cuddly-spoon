package com.tracking_delivery_system.tracking_service.service;

import com.tracking_delivery_system.tracking_service.model.Delivery;
import com.tracking_delivery_system.tracking_service.model.LocationUpdate;
import com.tracking_delivery_system.tracking_service.model.State;
import com.tracking_delivery_system.tracking_service.repository.LocationUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class DeliveryProgressSimulator implements DeliveryProgressSimulatorService {
    private final KafkaProducer kafkaProducer;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final LocationUpdateRepository locationUpdateRepository;
    private final ConcurrentMap<UUID, ScheduledFuture<?>> tasksMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Override
    public void simulateDeliverProgress(Delivery delivery) {
        UUID id = delivery.getId();
        LocationUpdate locationUpdate = locationUpdateRepository.findById(id)
                .orElseGet(() -> createNewLocationUpdate(delivery));

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (updateLocation(locationUpdate, delivery)) {
                ScheduledFuture<?> task = tasksMap.remove(id);
                if (task != null){
                    task.cancel(true);
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

        ScheduledFuture<?> existingTask = tasksMap.put(id, future);

        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
            tasksMap.remove(id);
        }
    }

    private LocationUpdate createNewLocationUpdate(Delivery deliveryRoute) {
        return new LocationUpdate(
                deliveryRoute.getId(),
                deliveryRoute.getStates().get(0).getName(),
                deliveryRoute.getStates().get(0).getCityName(),
                deliveryRoute.getDistance(),
                deliveryRoute.getDistance()
        );
    }

    private boolean updateLocation(LocationUpdate locationUpdate, Delivery delivery) {
        double remainingDistance = locationUpdate.getRemainingDistance();
        double totalDistance = locationUpdate.getTotalDistance();

        if (remainingDistance <= 0) {
            locationUpdate.setRemainingDistance(0);
            kafkaProducer.sendUpdate("location-update", locationUpdate);
            locationUpdateRepository.save(locationUpdate);
            return true;
        }

        remainingDistance -= calculateDistanceReduction(remainingDistance);
        locationUpdate.setRemainingDistance(remainingDistance);

        List<State> cities = delivery.getStates();
        locationUpdate.setCurrentState(cities.get(calculateCurrentCityIndex(remainingDistance, totalDistance, cities.size())).getName());
        locationUpdate.setCurrentCity(cities.get(calculateCurrentCityIndex(remainingDistance, totalDistance, cities.size())).getCityName());

        kafkaProducer.sendUpdate("location-update", locationUpdate);
        locationUpdateRepository.save(locationUpdate);

        return remainingDistance <= 0;
    }

    private int calculateCurrentCityIndex(double remainingDistance, double totalDistance, int numberOfCities) {
        double cityDistance = totalDistance / (numberOfCities - 1);

        int index = (int) Math.ceil((totalDistance - remainingDistance) / cityDistance);
        return Math.max(0, Math.min(index, numberOfCities - 1));
    }

    private double calculateDistanceReduction(double remainingDistance) {
        double baseReductionFactor = 0.05;
        double maxReduction = remainingDistance * baseReductionFactor;

        return Math.ceil(maxReduction * random.nextDouble());
    }
}
