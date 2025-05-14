package com.mc.weather.redis;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;


@Slf4j
@Service
public class WeatherPropertiesService {

    private final RedisTemplate<String, Object> redisTemplate;

    public WeatherPropertiesService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> saveProperties(Flux<Feature> features) {

        features = features.doOnNext(feature -> log.info("FEATURE RECEIVED: " + feature));

             return features
                    .publishOn(Schedulers.boundedElastic())
                     .flatMap(feature -> Mono.fromRunnable(() -> {
                                 Instant instant = Instant.parse(feature.properties().observed());
                                 Long observed = instant.getEpochSecond();
                                 String stationId = feature.properties().stationId();
                                 String key = RedisKeyBuilder.buildKey(
                                         stationId,
                                         feature.properties().parameterId(),
                                         observed);
                                 saveLatestTimestamp(stationId, observed);
                                 redisTemplate
                                         .opsForValue().set(key, feature);
                             }
                             )).then();

    }

    private void saveLatestTimestamp(String stationId, Long observed) {
        Integer lastObserved = getLastObserved(stationId);
        if (lastObserved == null || Integer.compare(observed.intValue(), lastObserved) > 0) {
            String lastUpdateKey = RedisKeyBuilder.buildKeyForLatestTimestamp(stationId);
            redisTemplate.opsForValue().set(lastUpdateKey, observed);
        }
    }

    public Integer getLastObserved(String stationId) {
        String key = RedisKeyBuilder.buildKeyForLatestTimestamp(stationId);
        return (Integer) redisTemplate.opsForValue().get(key);
    }
}
