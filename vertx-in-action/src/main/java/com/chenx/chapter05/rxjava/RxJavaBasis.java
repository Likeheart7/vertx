package com.chenx.chapter05.rxjava;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public class RxJavaBasis {
    private static final Logger log = LoggerFactory.getLogger(RxJavaBasis.class);
    public static void main(String[] args) {
        observable();
    }

    private static void observable() {
        // doOnSubscribe除外，其他都在5s后执行
        Observable
                .just("--", "this", "is", "--", "a", "sequence", "of", "items", "!")
                .doOnSubscribe(() -> System.out.println("Subscribed!")) // 插入操作，这里是当订阅发生
                .delay(5, TimeUnit.SECONDS) // 延迟5s
                .filter(s -> !s.startsWith("--"))   // 过滤
                .doOnNext(System.out::println)  // 调用流中的每个项目
                .map(String::toUpperCase)
                .buffer(2)  // 事件按2分组
                .subscribe(
                        System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println(">>> Done!") // complete时调用
                );

        // 防止主线程结束导致异步线程被停止，说明上面代码用的是守护线程
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            log.error("Interrupted Exception.");
        }
    }
}
