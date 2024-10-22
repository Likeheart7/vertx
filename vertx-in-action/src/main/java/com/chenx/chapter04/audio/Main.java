package com.chenx.chapter04.audio;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new JukeboxVerticle());
        vertx.deployVerticle(new NetControlVerticle());
    }
}
