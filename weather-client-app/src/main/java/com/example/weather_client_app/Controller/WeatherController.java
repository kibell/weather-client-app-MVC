package com.example.weather_client_app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.weather_client_app.service.WeatherService;

@Controller
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/weather")
    public String getWeather(@RequestParam(name = "lat", required = false, defaultValue = "38.8977") double latitude,
                             @RequestParam(name = "lon", required = false, defaultValue = "-77.0365") double longitude,
                             Model model) {
        try {
            String weatherData = weatherService.getWeather(latitude, longitude);
            model.addAttribute("weatherData", weatherData);
        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch weather data");
        }
        return "weather";
    }
}