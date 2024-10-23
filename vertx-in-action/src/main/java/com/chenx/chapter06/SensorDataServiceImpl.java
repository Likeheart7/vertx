package com.chenx.chapter06;

import generated.SensorDataService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SensorDataServiceImpl implements SensorDataService {
    private final HashMap<String, Double> lastValues = new HashMap<>();

    /**
     * 构造实例时会传入Vertx实例
     */
    public SensorDataServiceImpl(Vertx vertx) {
        // 为了获取HeatSensor更新的温度值，我们仍然需要订阅事件总线
        vertx.eventBus().<JsonObject>consumer("sensor.updates", message -> {
            JsonObject json = message.body();
            lastValues.put(json.getString("id"), json.getDouble("temp"));
        });
    }

    @Override
    public void valueFor(String sensorId, Handler<AsyncResult<JsonObject>> handler) {
        if (lastValues.containsKey(sensorId)) {
            JsonObject data = new JsonObject()
                    .put("sensorId", sensorId)
                    .put("value", lastValues.get(sensorId));
            // 使用异步结果，而不是传递消息来回复
            handler.handle(Future.succeededFuture(data));
        } else {
            handler.handle(Future.failedFuture("No value has been observed for " + sensorId));
        }
    }


    @Override
    public void average(Handler<AsyncResult<JsonObject>> handler) {
        Double avg = lastValues.values().stream().collect(Collectors.averagingDouble(Double::doubleValue));
        JsonObject data = new JsonObject().put("average", avg);
        // 使用异步结果来回复
        handler.handle(Future.succeededFuture(data));
    }
}
