package com.chenx.chapter05;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

public class HeatSensor extends AbstractVerticle {
    private double temperature = 21.0;
    private final Random random  = new Random();
    private final String sensorId = UUID.randomUUID().toString();

    private void scheduleNextUpdate() {
        // 随机1-6s更新速度
        vertx.setTimer(random.nextInt(5000) + 1000, this::update);
    }

    private void update(Long id) {
        temperature += delta() / 10;
        scheduleNextUpdate(); // 下次更新
    }

    private double delta() {
        if (random.nextInt() > 0) {
            return random.nextGaussian();
        } else {
            return -random.nextGaussian();
        }
    }

    /**
     * 默认在3000端口开启http监听，并第一次触发scheduleNextUpdate
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(this::requestHandler)
                .listen(config().getInteger("http.port", 3000));
        scheduleNextUpdate();
    }

    private void requestHandler(HttpServerRequest request) {
        JsonObject json = new JsonObject();
        json.put("id", sensorId)
                .put("temp", temperature);
        request.response()
                .putHeader("Context-Type", "application/json")
                .end(json.encode());
    }
}
