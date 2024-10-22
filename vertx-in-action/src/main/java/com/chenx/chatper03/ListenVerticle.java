package com.chenx.chatper03;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class ListenVerticle extends AbstractVerticle {
    private final Logger log = LoggerFactory.getLogger(ListenVerticle.class);
    private final DecimalFormat format = new DecimalFormat("#.##");

    @Override
    public void start() {
        EventBus bus = vertx.eventBus();
        // 订阅事件总线上的sensor.updates消息，打印日志
        bus.<JsonObject>consumer("sensor.updates", msg -> {
            JsonObject body = msg.body();
            String id = body.getString("id");
            String temp = format.format(body.getDouble("temp"));
            log.info("{} reports a temperature ~{}℃",id , temp);
        });
    }
}
