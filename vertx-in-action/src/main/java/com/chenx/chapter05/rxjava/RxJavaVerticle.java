package com.chenx.chapter05.rxjava;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.Vertx;
import rx.Completable;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * 使用Vertx-rx-java继承的RxJava的Verticle, 注意import 语句，这个实际上是vertx的AbstractVerticle的子类
 */
public class RxJavaVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        Observable
                .interval(1, TimeUnit.SECONDS, RxHelper.scheduler(vertx))
                .subscribe(n -> System.out.println("[" + Thread.currentThread().getName() + "] tick"));

        return vertx.createHttpServer()
                .requestHandler(r -> r.response().end("ok"))    // 响应ok
                .rxListen(8080) // RxJava的变体
                .toCompletable();
    }

    public static void main(String[] args) {
        Vertx vertx1 = Vertx.vertx();
        vertx1.deployVerticle(new RxJavaVerticle());
    }
}
