package com.mc.weather.dmi;

import com.mc.weather.data.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DmiApiService {

    @Value("${dmi.api.key}")
    private String apiKey;

    @Value("${dmi.api.base-url}")
    private String baseUrl;

    public WeatherResponse getObservations(String stationId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("%s/collections/observation/items?", baseUrl);
        return restTemplate.getForObject(url + "stationId={1}&api-key={2}", WeatherResponse.class, stationId, apiKey);
    }
}
