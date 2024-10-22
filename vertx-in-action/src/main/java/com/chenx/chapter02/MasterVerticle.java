package com.chenx.chapter02;

import com.sun.javafx.binding.SelectBinding;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过主Verticle部署其它Verticle
 */
public class MasterVerticle extends AbstractVerticle {
    public static final Logger log = LoggerFactory.getLogger(AbstractVerticle.class);
    @Override
    public void start() throws Exception {
        // 每隔2s部署一个SimpleVerticle
        int delay = 1000;
        for (int i = 0; i < 20; i++) {
            vertx.setTimer(delay, id -> {
                deploy();
            });
            delay += 1000;
        }
    }

    private void deploy() {
        vertx.deployVerticle(new SimpleVerticle(), res->{
            if (res.succeeded()) {
                String id = res.result();
                log.info("Successfully deploy [{}]", id);
                vertx.setTimer(5000, tid -> undeployLater(id));    // 5s后取消部署
            } else {
                log.error("Deploy failed, ", res.cause());
            }
        });
    }

    private void undeployLater(String id) {
        vertx.undeploy(id, ar->{
            if (ar.succeeded()) {
                log.info("Undeploy successfully.");
            } else {
                log.error("Undeploy failed.", ar.cause());
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MasterVerticle());
    }

    private static class SimpleVerticle extends AbstractVerticle {
        public static final Logger log = LoggerFactory.getLogger(SelectBinding.AsBoolean.class);
        @Override
        public void start() throws Exception {
            log.info("simple verticle start...");
        }

        @Override
        public void stop() throws Exception {
            log.info("simple verticle stop...");
        }
    }
}
