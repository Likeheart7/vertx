package com.chenx.chapter05;

import com.hazelcast.collection.impl.collection.CollectionService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 收集服务，整体逻辑是：
 * client -> server:8080 -> [post]server:3000+i -> server:8080(拿到数据)
 *   ⬆                                                   |
 *   <---------------------------------------------------|（发给快照服务）
 *          （快照服务成功，将数据响应给client）               ⬇
 *                                             server:4000(snapshot service)
 */
public class CollectorService extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(CollectionService.class);


    // 来自vertx-web模块，简化http处理
    private WebClient webClient;

    /**
     * 默认在8080端口开启监听
     */
    @Override
    public void start() throws Exception {
        webClient = WebClient.create(vertx);
        vertx.createHttpServer()
                .requestHandler(this::handleRequest)
                .listen(config().getInteger("http.port", 8080));
    }

    private void handleRequest(HttpServerRequest request) {
        // 用与收集json响应
        ArrayList<JsonObject> responses = new ArrayList<>();
        // 由于它们是异步的，我们还需要跟踪接收到的响应数量和响应值。
        // 出现错误的情况下响应数可能小于请求数量
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 3; i++) {
            // 向localhost:3000+i的/录井发送请求
            webClient.get(3000 + i, "localhost", "/")
                    .expect(ResponsePredicate.SC_SUCCESS)   // 如果状态码不是2xx，此谓词会触发错误
                    .as(BodyCodec.jsonObject()) // 将正文视为json对象并自动转换
                    .send(ar -> {
                        if (ar.succeeded()) {
                            responses.add(ar.result().body());
                        } else {
                            log.error("Sensor down?", ar.cause());
                        }
                        // 收到所有请求后，执行其他操作
                        if (counter.incrementAndGet() == 3) {
                            JsonObject data = new JsonObject();
                            data.put("data", new JsonArray(responses));
                            send2Snapshot(request, data);
                        }
                    });
        }
    }

    private void send2Snapshot(HttpServerRequest request, JsonObject data) {
        webClient
                .post(4000, "localhost", "/") // 发送post请求
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendJsonObject(data, ar -> {
                    if (ar.succeeded()) {
                        sendResponse(request, data);
                    } else {
                        log.error("Snapshot down?", ar.cause());
                        // 出现错误，以500响应码结束请求
                        request.response().setStatusCode(500).end();
                    }
                });
    }

    /**
     * post请求成功后的操作
     */
    private void sendResponse(HttpServerRequest request, JsonObject data) {
        // 注意这个request是客户端在8080端口上请求的request
        request.response()
                .putHeader("Content-Type", "application.json")
                .end(data.encode());
    }
}
