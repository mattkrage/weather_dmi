package com.mc.weather.dmi;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class DmiApiService {

    @Value("${dmi.api.key}")
    private String apiKey;

    @Value("${dmi.api.base-url}")
    private String baseUrl;

    @Autowired
    public WebClient webClient;

    public Flux<Feature> getObservations(String stationId, Integer lastObserved) {

        String from = lastObserved != null ? DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(lastObserved+1)) : "..";
        String dateTime = from+"/..";
        String url = String.format("%s/collections/observation/items?", baseUrl);

        Instant start = Instant.now();

        return webClient.get()
                        .uri(url + "stationId={1}&datetime={2}&api-key={3}", stationId, dateTime, apiKey)
                        .retrieve()
                .bodyToMono(WeatherResponse.class)
                .flatMapMany(response -> Flux.fromIterable(response.features()))
                .doFinally(signal -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("Reactive getObservations call took: {} ms", duration.toMillis());
                });
    }
}
