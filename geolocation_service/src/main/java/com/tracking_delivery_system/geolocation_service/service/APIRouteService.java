package com.tracking_delivery_system.geolocation_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracking_delivery_system.geolocation_service.model.Coordinates;
import com.tracking_delivery_system.geolocation_service.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class APIRouteService {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Value("${reverse.geocoding.api.url}")
    private String reverseGeocodingApiUrl;

    public Coordinates fetchCoordinates(String url) {
        try {
            JsonNode responseNode = fetchJsonFromUrl(url);
            if (responseNode.isEmpty()) {
                throw new IllegalArgumentException("City Not Found for URL: " + url);
            }
            return extractCoordinates(responseNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for coordinates. URL: " + url, e);
        }
    }

    private Coordinates extractCoordinates(JsonNode node) {
        JsonNode firstPlace = node.get(0);
        String lat = firstPlace.path("lat").asText();
        String lon = firstPlace.path("lon").asText();

        return new Coordinates(lat, lon);
    }

    public double fetchTotalDistance(String url) {
        try {
            JsonNode rootNode = fetchJsonFromUrl(url);
            return rootNode.path("routes").get(0).path("distance").asDouble();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for distance. URL: " + url, e);
        }
    }

    public List<State> fetchStates(String url) {
        try {
            JsonNode routesNode = fetchJsonFromUrl(url).path("routes");
            if (routesNode.isEmpty()) {
                throw new IllegalArgumentException("Route Not Found for URL: " + url);
            }
            return extractRouteCoordinates(routesNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for route coordinates. URL: " + url, e);
        }
    }

    private List<State> extractRouteCoordinates(JsonNode routesNode) {
        List<Coordinates> routeCoordinates = new ArrayList<>();
        JsonNode firstRoute = routesNode.get(0).path("geometry").path("coordinates");

        for (JsonNode coordinatePair : firstRoute) {
            String latitude = coordinatePair.get(1).asText();
            String longitude = coordinatePair.get(0).asText();
            routeCoordinates.add(new Coordinates(latitude, longitude));
        }

        return convertCoordinatesIntoStates(routeCoordinates);
    }

    private List<State> convertCoordinatesIntoStates(List<Coordinates> routeCoordinates) {
        List<State> states = new ArrayList<>();

        routeCoordinates.forEach(coordinate -> {
            String url = String.format(reverseGeocodingApiUrl, coordinate.latitude(), coordinate.longitude());
            Optional<State> state = fetchStateFromCoordinates(url);
            state.ifPresent(states::add);
        });

        return states;
    }

    private Optional<State> fetchStateFromCoordinates(String url) {
        try {
            JsonNode responseNode = fetchJsonFromUrl(url);
            return extractState(responseNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing coordinates for reverse geocoding. URL: " + url, e);
        }
    }

    private Optional<State> extractState(JsonNode node) {
        JsonNode stateNode = node.path("address").path("state");
        JsonNode cityNode = node.path("address").path("city");

        if (cityNode != null && !cityNode.asText().isEmpty()) {
            return Optional.of(new State(stateNode.asText(), cityNode.asText()));
        } else {
            return Optional.empty();
        }
    }

    private JsonNode fetchJsonFromUrl(String url) throws JsonProcessingException {
        try{
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return mapper.readTree(response.getBody());
        } catch (HttpClientErrorException e){
            throw new RuntimeException("Error fetching data from URL: " + url, e);
        }
    }
}
