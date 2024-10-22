package com.chenx.chatper03;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.TimeoutStream;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

public class HttpVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(this::handler)
                .listen(config().getInteger("port", 8080)); // 默认8080，可以通过verticle部署配置更改
    }

    private void handler(HttpServerRequest request) {
        if ("/".equals(request.path())) {
            // 请求根路径返回index.html文件
            request.response().sendFile("src/main/resources/static/index.html");
        } else if ("/sse".equals(request.path())) {
            // 请求/sse路径
            sse(request);
        } else {    //其他路径返回404响应码
            request.response().setStatusCode(404);
        }
    }

    /**
     * 通过vertx对server-sent events的实现，可以从服务器向客户端推送
     *
     * @param request
     */
    private void sse(HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response
                .putHeader("Content-Type", "text/event-stream")
                // 这是一个实施流，让浏览器不会缓存它
                .putHeader("Cache-Control", "no-cache")
                .setChunked(true);
        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer("sensor.updates");
        consumer.handler(msg -> {
            response.write("event:update\n");
            response.write("data: " + msg.body().encode() + "\n\n");
        });

        TimeoutStream ticks = vertx.periodicStream(1000);
        ticks.handler(id -> {
            // request发送一条期望的得到响应的消息，回复是一个异步对象，因为他可能已经失败
            // 请求-回复模式，这里发送，SensorData定于了sensor.average并回复
            vertx.eventBus().<JsonObject>request("sensor.average", "", reply -> {
                if (reply.succeeded()) {
                    response.write("event: average\n");
                    response.write("data: " + reply.result().body().encode() + "\n\n");
                }
            });
        });
        // 客户端断开连接/刷新页面，需要取消注册事件总线消息消费者，并取消计算平均值的周期任务
        response.endHandler(v -> {
            consumer.unregister();
            ticks.cancel();
        });
    }
}
