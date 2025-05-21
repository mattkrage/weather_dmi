package com.mc.weather.redis;

public class RedisKeyBuilder {

    public static String buildKey(String stationId, String parameterId, Long timestamp) {
        return String.format("weather:%s:%s:%d", stationId, parameterId, timestamp);
    }


    public static String buildKeyForLatestTimestamp(String stationId) {
        return "weather:station:" + stationId + ":latest";
    }

    public static String buildKeyForTimeSeries(String stationId, String parameterId) {
        return String.format("weather:timeseries:%s:%s", stationId, parameterId);
    }


}