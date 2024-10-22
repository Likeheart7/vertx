package com.chenx.chapter02;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ExecuteBlocking extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(ExecuteBlocking.class);

    @Override
    public void start() throws Exception {
        log.info("[{}] execute start method.", Thread.currentThread().getName());
        vertx.setPeriodic(3000, id -> {
            for (int i = 0; i < 3; i++) {
                vertx.executeBlocking(() -> {
                    // 可以看到是work线程执行的executeBlocking的回调
                    log.info("[{}] execute executing task.", Thread.currentThread().getName());
                    try {
                        log.info("before sleep..");
                        TimeUnit.SECONDS.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("after sleep..");
                    return "1";
                });
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ExecuteBlocking());
    }
}
