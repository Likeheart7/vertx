package com.chenx.chatper03;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // deploy 4 sensor
        vertx.deployVerticle(HeatSensor.class, new DeploymentOptions().setInstances(4));
        vertx.deployVerticle(new ListenVerticle());
        vertx.deployVerticle(new SensorData());
        vertx.deployVerticle(new HttpVerticle());
    }
}
