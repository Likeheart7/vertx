package com.chenx.chapter06;

import generated.SensorDataService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

/**
 * 测试
 */
@ExtendWith(VertxExtension.class) // vertx的junit5扩展
public class SensorDataServiceTest {
    // 服务代理引用
    private SensorDataService service;
    private SensorDataService dataService;
    private Checkpoint failToGet;

    // 在每个测试之前执行的逻辑
    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext context) {
        // 不是一个内部暴露服务的verticle，期待一个succeeding的部署
        vertx.deployVerticle(new DataVerticle(), context.succeeding(id -> {
            // 获取一个代理
            dataService = SensorDataService.createProxy(vertx, "sensor.data-service");
            context.completeNow(); // 通知设置完成
        }));
    }

    // VertxTestContext支持处理测试中的异步操作以报告成功/失败
    @Test
    void noSensor(VertxTestContext ctx) {
        // checkpoint永固确保异步操作在某一行通过
        Checkpoint failToGet = ctx.checkpoint();
        Checkpoint zeroAvg = ctx.checkpoint();

        // failing是Handler<AsyncResult>的助手，验证包装断言
        dataService.valueFor("abc", ctx.failing(err -> ctx.verify(() -> {
            assertThat(err.getMessage().startsWith("No value has been observed"));
            failToGet.flag();
        })));
        dataService.average(ctx.succeeding(data -> ctx.verify(() -> {
            Double avg = data.getDouble("average");
            assertThat(avg).isCloseTo(0.0d, withinPercentage(1.0d));
            zeroAvg.flag();
        })));
    }

    @Test
    void withSensors(Vertx vertx, VertxTestContext ctx) {
        Checkpoint getValue = ctx.checkpoint();
        Checkpoint goodAvg = ctx.checkpoint();

        // 模拟传感器消息
        JsonObject m1 = new JsonObject().put("id", "first").put("temp", 21.0);
        JsonObject m2 = new JsonObject().put("id", "second").put("temp", 27.0);

//        在总线发布消息
        vertx.eventBus()
                .publish("sensor.updates", m1)
                .publish("sensor.updates", m2);

        dataService.valueFor("first", ctx.succeeding(data -> ctx.verify(() -> {
            assertThat(data.getString("sensorId")).isEqualTo("first");
            assertThat(data.getDouble("value")).isEqualTo(21.0d);
            getValue.flag();
        })));

        dataService.average(ctx.succeeding(data -> ctx.verify(() -> {
            assertThat(data.getDouble("average")).isCloseTo(24.0, withinPercentage(1.0));
            goodAvg.flag();
        })));


    }

}
