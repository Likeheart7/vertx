package com.chenx.chapter05.promise;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// 将CompletableFuture转为vertx的Future
public class CompletableFuture2VertxFuture {
    public static void main(String[] args) {
        CompletableFuture<String> cs = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "5 seconds have elapsed";
        });
        Future
                .fromCompletionStage(cs, Vertx.vertx().getOrCreateContext())    // 转为vertx的Future并在vertx上下文调度
                .onSuccess(System.out::println)
                .onFailure(Throwable::printStackTrace);
    }
}
