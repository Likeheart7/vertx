package com.chenx.chapter06;


import generated.SensorDataService;
import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class DataVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        // 将事件总线服务部署到Verticle，并定义事件总线地址。
        new ServiceBinder(vertx)
                .setAddress("sensor.data-service")
                .register(SensorDataService.class, SensorDataService.create(vertx));
        vertx.setTimer(1000, id -> {
            SensorDataService service = SensorDataService.createProxy(vertx, "sensor.data-service");
            service.average(ar -> {
                if (ar.succeeded()) {
                    System.out.println("Average = " + ar.result());
                } else {
                    ar.cause().printStackTrace();
                }
            });
        });
    }
}
