package com.chenx.chatper03;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SensorData extends AbstractVerticle {
    // 通过唯一标识符，记录每个传感器的最新测量值
    private final HashMap<String, Double> lastValues = new HashMap<>();

    @Override
    public void start() throws Exception {
        EventBus bus = vertx.eventBus();
        // 说明了两个事件总线的消息处理程序
        bus.consumer("sensor.updates", this::update);
        bus.consumer("sensor.average", this::average);
    }

    /**
     * 接收到新的测量值，从JSON中提取数据，记录到lastValues中
     */
    private void update(Message<JsonObject> message) {
        JsonObject json = message.body();
        lastValues.put(json.getString("id"), json.getDouble("temp"));
    }

    private void average(Message<JsonObject> message) {
        Double avg = lastValues.values().stream()
                .collect(Collectors.averagingDouble(Double::doubleValue));
        JsonObject json = new JsonObject().put("average", avg);
        message.reply(json); // reply方法用于回复消息
    }
}
