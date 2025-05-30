package com.mc.weather.dmi;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.redis.WeatherPropertiesService;
import com.mc.weather.redis.WeatherRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DmiRetrieveDataJob {

    private static final Logger log = LoggerFactory.getLogger(DmiRetrieveDataJob.class);

    @Autowired
    private WeatherPropertiesService weatherPropertiesService;

    @Autowired
    private DmiApiService dmiApiService;

    @Autowired
    private WeatherRedisService weatherRedisService;

    public Mono<Void> syncWeatherForStation(String stationId) {
        return weatherPropertiesService.getLastObserved(stationId)
                .doOnNext(lastObserved -> log.info("Last observed: {}", lastObserved))
                .flatMap(lastObserved -> {
                    Flux<Feature> observations = dmiApiService.getObservations(stationId, lastObserved);
                    return weatherRedisService.saveWeatherData(observations);
                });
    }

    @Scheduled(fixedRate = 1000 * 60 * 5) // every 5 minutes
    public void taskEveryTwoSeconds() {
        log.info("Running DmiRetrieveDataJob...");
        syncWeatherForStation("06186").subscribe();
    }
}
