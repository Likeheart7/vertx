package com.chenx.chapter04;

import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;

public class BasicFileOpts {
    public static void main(String[] args) {
        fileRead();
    }

    /**
     * 基于vert.x的简单文件读取
     * 和传统的Java IO相比，原生的更像是拉数据，而这里更像是被推数据
     */
    private static void fileRead() {
        Vertx vertx = Vertx.vertx();
        OpenOptions opts = new OpenOptions().setRead(true);
        vertx.fileSystem().open("build.gradle.kts", opts, ar -> {
            if (ar.succeeded()) {
                AsyncFile af = ar.result();
                af.handler(System.out::println) // 新缓冲区数据的回调
                        .exceptionHandler(Throwable::printStackTrace) // 异常回调
                        .endHandler(v -> {  // 结束回调
                            System.out.println("\n-- DONE!");
                            vertx.close();
                        });
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}
