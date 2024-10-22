package com.chenx.chapter03;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

public class HeatSensor extends AbstractVerticle {
    private final Random random = new Random();
    private final String sensorId = UUID.randomUUID().toString();
    private double temperature = 21.0;

    @Override
    public void start() throws Exception {
        scheduleNextUpdate();
    }

    /**
     * 1-6s的随机延迟
     */
    private void scheduleNextUpdate() {
        vertx.setTimer(random.nextInt(5000) + 1000, this::update);
    }

    private void update(Long timerId) {
        temperature += (delta() / 10);
        JsonObject payload = new JsonObject()
                .put("id", sensorId)
                .put("temp", temperature);
        // 在总线上发布消息，订阅sensor.updates的verticle会收到消息
        vertx.eventBus().publish("sensor.updates", payload);
        scheduleNextUpdate();   // 下一次更新
    }

    /**
     * 根据随机值对温度少量的加减
     */
    private double delta() {
        if (random.nextInt() > 0) {
            return random.nextGaussian();
        } else {
            return -random.nextGaussian();
        }
    }
}
