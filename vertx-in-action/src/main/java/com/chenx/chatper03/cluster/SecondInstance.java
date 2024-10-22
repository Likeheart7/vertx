package com.chenx.chatper03.cluster;

import com.chenx.chatper03.HeatSensor;
import com.chenx.chatper03.HttpVerticle;
import com.chenx.chatper03.ListenVerticle;
import com.chenx.chatper03.SensorData;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondInstance {
    private static final Logger log = LoggerFactory.getLogger(SecondInstance.class);

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(), ar -> {
            if (ar.succeeded()) {
                log.info("Second instance has been started.");
                Vertx vertx = ar.result();
                vertx.deployVerticle(HeatSensor.class, new DeploymentOptions().setInstances(8));
                vertx.deployVerticle(new ListenVerticle());
                vertx.deployVerticle(new SensorData());
                // HttpVerticle可以通过配置更改端口
                JsonObject conf = new JsonObject().put("port", 8081);
                vertx.deployVerticle(HttpVerticle.class, new DeploymentOptions().setConfig(conf));
            } else {
                log.error("Could not start.");
            }
        });
    }
}
