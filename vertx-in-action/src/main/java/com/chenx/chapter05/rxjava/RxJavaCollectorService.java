package com.chenx.chapter05.rxjava;

import com.chenx.chapter05.CollectorService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.client.predicate.ResponsePredicate;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Single;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 使用RxJava来重构CollectorService
 */
public class RxJavaCollectorService extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(RxJavaCollectorService.class);
    private WebClient webClient;

    @Override
    public Completable rxStart() {
        webClient = WebClient.create(vertx);
        return vertx.createHttpServer()
                .requestHandler(this::handleRequest)
                .rxListen(8080)
                .toCompletable();
    }

    /**
     * 使用RxJava重构{@link CollectorService#handleRequest(io.vertx.core.http.HttpServerRequest)}
     */
    private void handleRequest(HttpServerRequest request) {
        Single<JsonObject> data = collectTemperature();
        send2Snapshot(data) // 数据发给快照服务
                .subscribe(json -> request.response()
                                .putHeader("Content-Type", "application/json")
                                .end(json.encode())
                        , err -> {
                            log.error("Something went wrong", err);
                            request.response().setStatusCode(500).end();
                        });
    }

    private Single<HttpResponse<JsonObject>> fetchTemperature(int port) {
        return webClient
                .get(port, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .as(BodyCodec.jsonObject())
                .rxSend();  // 会返回一个Single
    }

    private Single<JsonObject> collectTemperature() {
        Single<HttpResponse<JsonObject>> r1 = fetchTemperature(3000);
        Single<HttpResponse<JsonObject>> r2 = fetchTemperature(3001);
        Single<HttpResponse<JsonObject>> r3 = fetchTemperature(3002);
        return Single.zip(r1, r2, r3, (j1, j2, j3) -> { // zip运算包含三个响应
            JsonArray array = new JsonArray()
                    .add(j1.body())
                    .add(j2.body())
                    .add(j3.body());
            return new JsonObject().put("data", array); // 该值是打包在Single中的zip操作符结果
        });
    }

    private Single<JsonObject> send2Snapshot(Single<JsonObject> data) {
        // 在组合异步顺序操作的情况下，可以将flatMap视为andThen
        return data.flatMap(json -> webClient
                .post(4000, "localhost", "")
                .expect(ResponsePredicate.SC_SUCCESS)
                .rxSendJsonObject(json) // 发送json对象，然后报告http请求响应
                .flatMap(resp -> Single.just(json)));   // 返回json对象
    }
}
