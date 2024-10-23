package com.chenx.chapter05.promise;

import com.chenx.chapter05.CollectorService;
import com.hazelcast.collection.impl.collection.CollectionService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 使用Future重构CollectorServer
 */
public class CollectorServiceFuture extends AbstractVerticle {
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

    /**
     * 使用Futrue重构{@link CollectorService#handleRequest(HttpServerRequest)}
     */
    private void handleRequest(HttpServerRequest request) {
        // 通过Future.all组合三个异步任务
        Future.all(
                        fetchTemperature(3000),
                        fetchTemperature(3001),
                        fetchTemperature(3002)
                )
                // 会在三个异步任务都完成后执行，flatMap可以视为andThen
                .flatMap(this::send2Snapshot)
                // send2Snapshot执行成功，执行响应
                .onSuccess(data -> request.response()
                        .putHeader("Content-Type", "application/json")
                        .end(data.encode()));
    }

    private Future<JsonObject> fetchTemperature(int port) {
        // 请求指定端口的HeatSensor服务
        return webClient.get(port, "localhost", "/")
                .as(BodyCodec.jsonObject())
                .send()
                .expecting(HttpResponseExpectation.SC_SUCCESS)
                .map(HttpResponse::body);
    }

    private Future<JsonObject> send2Snapshot(CompositeFuture temps) {
        List<JsonObject> tempData = temps.list(); // 获取所有结果
        // 将结果转为JsonObject中的JsonArray
        JsonObject data = new JsonObject()
                .put("data", new JsonArray()
                        .add(tempData.get(0))
                        .add(tempData.get(1))
                        .add(tempData.get(2))
                );
        // 将转换后的JsonObject发送到SnapShotServer
        return webClient.post(4000, "localhost", "/")
                .sendJsonObject(data)
                .expecting(HttpResponseExpectation.SC_SUCCESS)
                .map(response -> data);
    }

}
