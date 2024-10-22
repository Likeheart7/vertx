package com.chenx.chapter02;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassingConfigVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(PassingConfigVerticle.class);

    @Override
    public void start() throws Exception {
        log.info("n = {}", config().getInteger("n", -1));
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        for (int i = 0; i < 4; i++) {
            JsonObject conf = new JsonObject().put("n", i);
            DeploymentOptions opts = new DeploymentOptions()
                    .setConfig(conf)    // 设置配置
                    .setInstances(i);   // 可以一次部署多个实例
            // 一次部署多个实例就不能用传递一个对象实例了，传递class对象时，setInstance不能<1
            vertx.deployVerticle("com.chenx.chapter02.PassingConfigVerticle", opts);
        }
    }
}
