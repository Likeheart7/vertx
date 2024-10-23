package com.chenx.chapter05;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotServer extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(SnapshotServer.class);

    /**
     * 默认在4000端口开启一个快照服务监听
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(req -> {
                    if (badRequest(req)) {
                        req.response().setStatusCode(400).end();
                    }
                    // bodyHandler会等待整个主体被接收，而不是组装中间缓冲区
                    req.bodyHandler(buffer-> {
                        log.info("Latest temperature: {}", buffer.toJsonObject().encodePrettily());
                        req.response().end();
                    });
                })
                .listen(config().getInteger("http.port", 4000));
    }

    /**
     * 请求必须是POST，并且必须是application/json
     */
    private boolean badRequest(HttpServerRequest req) {
       return !req.method().equals(HttpMethod.POST) ||
               !"application/json".equals(req.getHeader("Content-Type"));
    }
}
