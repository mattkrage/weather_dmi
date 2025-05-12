package com.mc.weather.dmi;

import com.mc.weather.data.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Service
public class DmiApiService {

    @Value("${dmi.api.key}")
    private String apiKey;

    @Value("${dmi.api.base-url}")
    private String baseUrl;

    public WeatherResponse getObservations(String stationId, Integer lastObserved) {

        String from = lastObserved != null ? DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(lastObserved+1)) : "..";
        String dateTime = from+"/..";

        System.out.println("LAST OBSERVED2:" +dateTime);

        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("%s/collections/observation/items?", baseUrl);
        return restTemplate.getForObject(url + "stationId={1}&datetime={2}&api-key={3}", WeatherResponse.class, stationId, dateTime, apiKey);
    }
}
