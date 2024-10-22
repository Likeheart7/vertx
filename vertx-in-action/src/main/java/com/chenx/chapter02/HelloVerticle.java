package com.chenx.chapter02;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * vertx的核心概念之Verticle
 */
public class HelloVerticle extends AbstractVerticle {
    private final Logger log = LoggerFactory.getLogger(HelloVerticle.class);
    private long counter = 1;

    @Override
    public void start(Promise<Void> promise) throws Exception {
        // 定义一个定时任务
        vertx.setPeriodic(5000, id -> {
            log.info("tick");
        });
        vertx.createHttpServer()
                .requestHandler(req -> {
                    log.info("Request #{} from {}", counter++, req.remoteAddress().host());
                    log.info("request path: [{}]", req.path());
                    req.response().end("Hey! Hi! Hello!");
                })
                .listen(8080, res -> {  // 根据启动结果向promise设置响应
                    if (res.succeeded()) {
                        promise.complete();
                    } else if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });
        log.info("Open http://localhost:8080/");
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HelloVerticle());
    }
}
