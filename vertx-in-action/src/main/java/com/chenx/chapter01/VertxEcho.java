package com.chenx.chapter01;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

public class VertxEcho {
    // 事件处理总在一个线程执行，所以无需并发控制 / AtomicInteger
    public static int numberOfConnection = 0;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.createNetServer() // 创建HTTP服务器
                .connectHandler(VertxEcho::handleNewClient) // 连接处理器回调方法
                .listen(8333);  // 监听8333端口

        // 定义一个定时任务，每5s执行一次回调函数
        vertx.setPeriodic(5000, id -> System.out.println(howMany()));

        // 创建HTTP服务器，设置监听端口并为每个请求提供回调函数
        vertx.createHttpServer()
                .requestHandler(request -> request.response().end(howMany()))
                .listen(8080);
    }

    /**
     * 处理新的连接
     */
    private static void handleNewClient(NetSocket socket) {
        // 连接数增加
        numberOfConnection++;
        // 响应原内容
        socket.handler(buffer -> {
            socket.write(buffer);
            // 如果是/quit，关闭连接
            if (buffer.toString().endsWith("/quit\n")) {
                socket.close();
            }
        });
        // 关闭连接的回调，连接数减 1
        socket.closeHandler(v -> numberOfConnection--);
    }

    private static String howMany() {
        return "现在有 " + numberOfConnection + " 个连接";
    }
}
