package com.chenx.chapter02;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 组合第三方非Vertx线程
 */
public class MixedDiffThreadModel extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(MixedDiffThreadModel.class);
    @Override
    public void start() {
        // start在事件循环线程运行，获我们获取该verticle的context
        Context context = vertx.getOrCreateContext();
        // 启动一个普通Java线程
        new Thread(() -> {
            try {
                run(context);
            } catch (InterruptedException e) {
                log.error("Oops", e);
            }
        }).start();
    }

    private void run(Context context) throws InterruptedException{
        CountDownLatch countDownLatch = new CountDownLatch(1);
        log.info("Now, I am in a non-vertx thread. [{}]", Thread.currentThread().getName());
        context.runOnContext(v -> {
            log.info("I am in a vertx thread. [{}]", Thread.currentThread().getName());
            vertx.setTimer(2000, id ->{
                log.info("this is final countdown.");
                countDownLatch.countDown();
            });
        });
        log.info("Waiting on the countDownLatch...");
        countDownLatch.await();
        log.info("Goodbye!");
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MixedDiffThreadModel());
    }

}
