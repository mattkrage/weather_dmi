package com.mc.weather.redis;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;


@Slf4j
@Service
public class WeatherPropertiesService {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public WeatherPropertiesService(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.reactiveRedisTemplate = redisTemplate;
    }

    public Mono<Void> saveProperties(Flux<Feature> features) {

        return features
                .doOnNext(feature -> log.info("FEATURE RECEIVED: " + feature))
                .flatMap(this::saveFeatureReactive) // chain reactive method
                .then();
    }

    private Mono<Void> saveFeatureReactive(Feature feature) {
        Instant instant = Instant.parse(feature.properties().observed());
        long observed = instant.getEpochSecond();
        String stationId = feature.properties().stationId();
        String paramId = feature.properties().parameterId();

        String key = RedisKeyBuilder.buildKey(stationId, paramId, observed);
        String tsKey = RedisKeyBuilder.buildKeyForTimeSeries(stationId, paramId);
        String lastUpdateKey = RedisKeyBuilder.buildKeyForLatestTimestamp(stationId);

        Mono<Boolean> saveFeature = reactiveRedisTemplate.opsForValue().set(key, feature);

        Mono<Boolean> saveTimeSerie = reactiveRedisTemplate
                .opsForZSet()
                .add(tsKey, feature.properties().value(), observed);

        Mono<Void> saveLatest = updateIfNewTimestampReactive(stationId, observed, lastUpdateKey);

        return Mono.when(saveFeature, saveTimeSerie, saveLatest).then();
    }

    private Mono<Void> updateIfNewTimestampReactive(String stationId, long observed, String key) {
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(Number.class)
                .defaultIfEmpty(0)
                .flatMap(existing -> {
                    if (observed > existing.longValue()) {
                        return reactiveRedisTemplate.opsForValue().set(key, observed).then();
                    } else {
                        return Mono.empty(); // do nothing
                    }
                });
    }


    public Mono<Integer> getLastObserved(String stationId) {
        String key = RedisKeyBuilder.buildKeyForLatestTimestamp(stationId);
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .map(obj -> {
                    if (obj instanceof Integer) return (Integer) obj;
                    if (obj instanceof Long) return ((Long) obj).intValue();
                    return null;
                })
                .defaultIfEmpty(0);
    }
}
