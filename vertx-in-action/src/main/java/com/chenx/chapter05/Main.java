package com.chenx.chapter05;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // 首先在3000、3001、3002部署3个HeatSensor服务
        vertx.deployVerticle(
                HeatSensor.class,
                new DeploymentOptions()
                        .setConfig(new JsonObject().put("http.port", 3000))
        );
        vertx.deployVerticle(
                HeatSensor.class,
                new DeploymentOptions()
                        .setConfig(new JsonObject().put("http.port", 3001))
        );
        vertx.deployVerticle(
                HeatSensor.class,
                new DeploymentOptions()
                        .setConfig(new JsonObject().put("http.port", 3002))
        );

        // 部署快照服务和采集服务
        vertx.deployVerticle(new SnapshotServer());
        vertx.deployVerticle(new CollectorService());
    }
}