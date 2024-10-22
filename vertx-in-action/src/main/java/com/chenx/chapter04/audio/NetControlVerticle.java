package com.chenx.chapter04.audio;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import jdk.nashorn.internal.runtime.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * 用于接受文本命令控制点唱机播放的内容
 * 我们定义，在基于TCP的文本协议命令格式如下
 * /action [argument]
 * action包括：
 *  /list - 列出所有可播放文件
 *  /play - 播放流
 *  /pause - 暂停流
 *  /schedule file - 在播放列表追加file
 *  每个文本行只能有一个命令，按行分割
 *
 *  根据以上目的，我们需要解决的问题：
 *      1. 解析器：缓冲区以块的形式到达，所以有粘包问题
 * </pre>
 */
public class NetControlVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(NetControlVerticle.class);

    @Override
    public void start() throws Exception {
        vertx.createNetServer()
                .connectHandler(this::handleClient)
                // 监听3000端口
                .listen(3000);
    }

    private void handleClient(NetSocket socket) {
        // 使用vertx提供的RecordBuffer处理粘包
        RecordParser.newDelimited("\n", socket)
                // 这里拿到的buffer是一行
                .handler(buffer -> handleBuffer(socket, buffer))
                .endHandler(v -> log.info("Connection ended."));
    }

    /**
     * 处理缓冲区
     *
     * @param socket 套接字
     * @param buffer 缓冲区
     */
    private void handleBuffer(NetSocket socket, Buffer buffer) {
        // 解码buffer
        String command = buffer.toString();
        switch (command) {
            case "/list":
                listCommand(socket);
                break;
            //对于 play和pause，直接向事件总线发送消息让JukeboxVerticle处理
            case "/play":
                vertx.eventBus().send("jukebox.play", "");
                break;
            case "/pause":
                vertx.eventBus().send("jukebox.pause", "");
                break;
            default:
                if (command.startsWith("/schedule ")) {
                    schedule(command);
                } else {
                    socket.write("Unknown command\n");
                }
        }
    }

    private void schedule(String command) {
        // 将/schedule 去掉
        String track = command.substring(10);
        JsonObject jsonObject = new JsonObject().put("file", track);
        vertx.eventBus().send("jukebox.schedule", jsonObject);
    }

    private void listCommand(NetSocket socket) {
        // 请求事件总线，JukeboxVerticle中会回复
        vertx.eventBus().request("jukebox.list", "", reply -> {
            if (reply.succeeded()) {
                JsonObject data = (JsonObject) reply.result().body();
                // 将每个文件名写出
                data.getJsonArray("files").stream()
                        .forEach(name -> socket.write(name + "\n"));
            } else {
                log.error("/list error", reply.cause());
            }
        });
    }

}
