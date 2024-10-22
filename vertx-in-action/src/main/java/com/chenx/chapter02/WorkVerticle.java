package com.chenx.chapter02;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * work verticle工作线程，允许阻塞而不被线程检查器警告
 */
public class WorkVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(WorkVerticle.class);

    @Override
    public void start() throws Exception {
        vertx.setPeriodic(3000, id -> {
            try {
                log.info("[{}] start sleep...", id);
                TimeUnit.SECONDS.sleep(8);
                log.info("[{}] stop sleep...", id);
            } catch (InterruptedException e) {
                log.error("Oops.", e.getCause());
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions opts = new DeploymentOptions().setInstances(2).setThreadingModel(ThreadingModel.WORKER);
        vertx.deployVerticle(WorkVerticle.class, opts);
    }
}
