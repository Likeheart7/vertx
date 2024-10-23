package com.chenx.chapter05.promise;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromiseBasis {
    private static final Logger log = LoggerFactory.getLogger(PromiseBasis.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Promise<String> promise = Promise.promise();
        vertx.setTimer(5000, id -> {
            if (System.currentTimeMillis() % 2L == 0L) {
                promise.complete("ok, i am lucky!");
            } else {
                promise.fail(new RuntimeException("oops, we failed."));
            }
        });
        Future<String> future = promise.future();
        // 针对promise的complete和fail的处理
//        future
//                .onSuccess(System.out::println)
//                .onFailure(err -> {
//                    log.error("something wrong.", err.getCause());
//                });
        // 也可以通过recover处理掉fail的情况
        future.recover(err -> Future.succeededFuture("ok, although there are some wrong, but we recover it."))
                .map(String::toUpperCase)
                .flatMap(str -> {
                    Promise<String> next = Promise.promise();
                    vertx.setTimer(2000, id->next.complete(">>> " + str));
                    return next.future();
                })
                .onSuccess(System.out::println);
    }
}
