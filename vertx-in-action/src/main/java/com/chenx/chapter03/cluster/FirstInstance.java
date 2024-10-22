package com.chenx.chapter03.cluster;

import com.chenx.chapter03.HeatSensor;
import com.chenx.chapter03.HttpVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
集群需要引入集群管理器的依赖，可以用vertx-hazelcast，版本和vertx-core一致
引入后发现两个集群部署的所有实例两个集群都可以访问到，这对上层来说完全是透明的
 */
public class FirstInstance {
    private static final Logger log = LoggerFactory.getLogger(FirstInstance.class);

    public static void main(String[] args) {
        // 启动集群vertx是一个异步操作
        Vertx.clusteredVertx(new VertxOptions(), ar -> {
            if (ar.succeeded()) {
                log.info("First instance has been started.");
                Vertx vertx = ar.result();  // 启动成功后获取vertx实例
                vertx.deployVerticle(HeatSensor.class, new DeploymentOptions().setInstances(4));  // deploy 4 sensor
                vertx.deployVerticle(new HttpVerticle());   // deploy 1 http server verticle
            } else {
                log.error("Could not start", ar.cause());   // 潜在原因可能是缺少集群管理器库
            }
        });
    }
}
