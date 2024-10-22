package com.chenx.starter;

import io.vertx.core.Vertx;

import java.util.concurrent.TimeUnit;

public class BlockExecutionEx {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    // ordered参数表示是否有序执行，默认为true
    vertx.executeBlocking(() -> {
      try {
        TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("task 1 success");
        return 1;
      }, false
    );
    vertx.executeBlocking(() -> {
        System.out.println("task 2 success");
        return 2;
      }, false
    );
    vertx.executeBlocking(() -> {
        System.out.println("task 3 success");
        return 3;
      }, false
    );
    vertx.executeBlocking(() -> {
        System.out.println("task 4 success");
        return 4;
      }, false
    );
    vertx.executeBlocking(() -> {
        System.out.println("task 5 success");
        return 5;
      }, false
    );
  }
}
