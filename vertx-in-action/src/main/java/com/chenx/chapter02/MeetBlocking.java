package com.chenx.chapter02;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class MeetBlocking extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        /*
        当事件处理程序遇到阻塞or长时间代码（默认阈值2s）时，线程检查器会告警，最终会抛出异常
        可以通过系统属性更改
            -Dvertx.options.blockedThreadCheckInterval=5000 将时间间隔更改为5秒。
            -Dvertx.threadChecks=false 禁用线程检查器。
        WARN [vertx-blocked-thread-checker] BlockedThreadChecker - Thread Thread[vert.x-eventloop-thread-0,5,main] has been blocked for 2202 ms, time limit is 2000 ms
        WARN [vertx-blocked-thread-checker] BlockedThreadChecker - Thread Thread[vert.x-eventloop-thread-0,5,main] has been blocked for 3216 ms, time limit is 2000 ms
        WARN [vertx-blocked-thread-checker] BlockedThreadChecker - Thread Thread[vert.x-eventloop-thread-0,5,main] has been blocked for 4227 ms, time limit is 2000 ms
        WARN [vertx-blocked-thread-checker] BlockedThreadChecker - Thread Thread[vert.x-eventloop-thread-0,5,main] has been blocked for 5231 ms, time limit is 2000 ms
        io.vertx.core.VertxException: Thread blocked
         */
        vertx.setPeriodic(5000, id -> {
            while (true);
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MeetBlocking());
    }
}
