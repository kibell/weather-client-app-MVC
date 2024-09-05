package com.example.weather_client_app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getWeather(double latitude, double longitude) {
        String gridPointsUrl = String.format("https://api.weather.gov/points/%f,%f", latitude, longitude);
        try {
            GridPointsResponse gridPointsResponse = restTemplate.getForObject(gridPointsUrl, GridPointsResponse.class);
            if (gridPointsResponse != null && gridPointsResponse.properties != null) {
                String office = gridPointsResponse.properties.gridId;
                int gridX = gridPointsResponse.properties.gridX;
                int gridY = gridPointsResponse.properties.gridY;
                return getForecast(office, gridX, gridY);
            } else {
                throw new RuntimeException("Invalid response from grid points API");
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error fetching grid points", e);
        }
    }

    private String getForecast(String office, int gridX, int gridY) {
        String forecastUrl = String.format("https://api.weather.gov/gridpoints/%s/%d,%d/forecast", office, gridX, gridY);
        try {
            String forecastResponse = restTemplate.getForObject(forecastUrl, String.class);
            return extractWeatherData(forecastResponse);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error fetching forecast", e);
        }
    }

    private String extractWeatherData(String forecastResponse) {
        try {
            JsonNode root = objectMapper.readTree(forecastResponse);
            JsonNode periods = root.path("properties").path("periods");
            StringBuilder weatherData = new StringBuilder();
            for (JsonNode period : periods) {
                String name = period.path("name").asText();
                String temperature = period.path("temperature").asText();
                String temperatureUnit = period.path("temperatureUnit").asText();
                String detailedForecast = period.path("detailedForecast").asText();
                weatherData.append(String.format("%s: %s%s - %s%n", name, temperature, temperatureUnit, detailedForecast));
            }
            return weatherData.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error parsing forecast response", e);
        }
    }

    private static class GridPointsResponse {
        public Properties properties;

        public static class Properties {
            public String gridId;
            public int gridX;
            public int gridY;
        }
    }
}