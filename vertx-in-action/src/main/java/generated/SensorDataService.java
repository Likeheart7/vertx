package generated;

import com.chenx.chapter06.SensorDataServiceImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx;


// 因为必须和使用@MuduleGen注解的package-info.java文件在同一个目录下，所以放在这里
@ProxyGen // 该注解用于生成总线代理
public interface SensorDataService {
    /**
     * 创建服务实例的工厂方法
     */
    static SensorDataService create(Vertx vertx) {
        return new SensorDataServiceImpl(vertx);
    }

    /**
     * 创建代理的工厂方法
     */
    static SensorDataService createProxy(Vertx vertx, String address) {
        return new SensorDataServiceVertxEBProxy(vertx, address);
    }

    /**
     * 接收参数和回调函数操作
     */
    void valueFor(String sensorId, Handler<AsyncResult<JsonObject>> handler);

    /**
     * 不带参数，有一个回到函数操作
     */
    void average(Handler<AsyncResult<JsonObject>> handler);

}
